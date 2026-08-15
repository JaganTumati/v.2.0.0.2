import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { MenuCategory, MenuItem } from '../lib/types';
import { MenuItemForm } from '../components/MenuItemForm';

interface MenuItemPayload {
  categoryId: string;
  name: string;
  description: string;
  price: number;
  vegetarian: boolean;
  spicyLevel: number;
  available: boolean;
}

export function MenuPage() {
  const queryClient = useQueryClient();
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  const [formMode, setFormMode] = useState<'create' | 'edit' | null>(null);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);

  const categories = useQuery({
    queryKey: ['menu-categories'],
    queryFn: () => api.get<MenuCategory[]>('/api/menu-categories'),
  });

  const items = useQuery({
    queryKey: ['menu-items', activeCategory],
    queryFn: () =>
      api.get<MenuItem[]>(
        `/api/menu-items?includeUnavailable=true${activeCategory ? `&categoryId=${activeCategory}` : ''}`,
      ),
  });

  const invalidateItems = () => queryClient.invalidateQueries({ queryKey: ['menu-items'] });

  const toggleAvailability = useMutation({
    mutationFn: ({ id, available }: { id: string; available: boolean }) =>
      api.patch(`/api/menu-items/${id}/availability`, { available }),
    onSuccess: invalidateItems,
  });

  const createItem = useMutation({
    mutationFn: (payload: MenuItemPayload) => api.post('/api/menu-items', payload),
    onSuccess: () => {
      invalidateItems();
      setFormMode(null);
    },
  });

  const updateItem = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: MenuItemPayload }) =>
      api.put(`/api/menu-items/${id}`, payload),
    onSuccess: () => {
      invalidateItems();
      setFormMode(null);
      setEditingItem(null);
    },
  });

  function openCreate() {
    setEditingItem(null);
    setFormMode('create');
  }

  function openEdit(item: MenuItem) {
    setEditingItem(item);
    setFormMode('edit');
  }

  function closeForm() {
    setFormMode(null);
    setEditingItem(null);
  }

  return (
    <div>
      <div className="flex items-start justify-between">
        <div>
          <h1 className="font-display text-xl font-semibold text-ink">Menu</h1>
          <p className="mt-1 text-sm text-slate">
            What the AI is allowed to sell right now. Toggling an item off makes it
            unavailable to customers immediately.
          </p>
        </div>
        <button
          onClick={openCreate}
          disabled={!categories.data || categories.data.length === 0}
          className="shrink-0 rounded-sm bg-marigold px-4 py-2 text-sm font-semibold text-charcoal hover:opacity-90 disabled:opacity-50"
        >
          + New item
        </button>
      </div>

      <div className="mt-5 flex gap-1.5">
        <button
          onClick={() => setActiveCategory(null)}
          className={`rounded-sm px-3 py-1.5 text-xs font-medium transition-colors ${
            activeCategory === null ? 'bg-charcoal text-paper' : 'bg-white text-slate hover:bg-paper-dim'
          }`}
        >
          All categories
        </button>
        {categories.data?.map((c) => (
          <button
            key={c.id}
            onClick={() => setActiveCategory(c.id)}
            className={`rounded-sm px-3 py-1.5 text-xs font-medium transition-colors ${
              activeCategory === c.id ? 'bg-charcoal text-paper' : 'bg-white text-slate hover:bg-paper-dim'
            }`}
          >
            {c.name}
          </button>
        ))}
      </div>

      <div className="mt-5 grid grid-cols-1 gap-3 md:grid-cols-2">
        {items.data?.map((item) => (
          <div
            key={item.id}
            className={`rounded-md border bg-white p-4 ${
              item.available ? 'border-paper-dim' : 'border-paper-dim opacity-60'
            }`}
          >
            <div className="flex items-start justify-between gap-3">
              <button onClick={() => openEdit(item)} className="min-w-0 text-left">
                <p className="truncate text-sm font-semibold text-ink hover:underline">{item.name}</p>
                <p className="mt-0.5 line-clamp-2 text-xs text-slate">{item.description}</p>
              </button>
              <p className="shrink-0 font-ticket text-sm font-medium text-ink">
                ₹{item.price.toFixed(0)}
              </p>
            </div>

            <div className="mt-3 flex items-center justify-between">
              <div className="flex gap-1.5">
                {item.vegetarian && (
                  <span className="rounded-sm bg-teal-soft px-1.5 py-0.5 font-ticket text-[10px] uppercase text-teal">
                    Veg
                  </span>
                )}
                {item.spicyLevel > 0 && (
                  <span className="rounded-sm bg-brick-soft px-1.5 py-0.5 font-ticket text-[10px] uppercase text-brick">
                    {'🌶'.repeat(item.spicyLevel)}
                  </span>
                )}
              </div>

              <button
                onClick={() =>
                  toggleAvailability.mutate({ id: item.id, available: !item.available })
                }
                className={`rounded-sm px-2.5 py-1 font-ticket text-[10px] font-medium uppercase tracking-wide transition-colors ${
                  item.available
                    ? 'bg-teal-soft text-teal hover:bg-teal/20'
                    : 'bg-paper-dim text-slate hover:bg-paper-dim/70'
                }`}
              >
                {item.available ? 'Available' : 'Unavailable'}
              </button>
            </div>
          </div>
        ))}
        {items.data?.length === 0 && (
          <p className="text-sm text-slate">No items in this category.</p>
        )}
      </div>

      {formMode && categories.data && (
        <MenuItemForm
          categories={categories.data}
          initial={editingItem ?? undefined}
          submitting={createItem.isPending || updateItem.isPending}
          onCancel={closeForm}
          onSubmit={(payload) => {
            if (formMode === 'edit' && editingItem) {
              updateItem.mutate({ id: editingItem.id, payload });
            } else {
              createItem.mutate(payload);
            }
          }}
        />
      )}
    </div>
  );
}
