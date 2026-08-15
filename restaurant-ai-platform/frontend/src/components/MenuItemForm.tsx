import { FormEvent, useState } from 'react';
import { MenuCategory, MenuItem } from '../lib/types';

interface MenuItemFormProps {
  categories: MenuCategory[];
  initial?: MenuItem;
  onSubmit: (values: {
    categoryId: string;
    name: string;
    description: string;
    price: number;
    vegetarian: boolean;
    spicyLevel: number;
    available: boolean;
  }) => void;
  onCancel: () => void;
  submitting: boolean;
}

export function MenuItemForm({ categories, initial, onSubmit, onCancel, submitting }: MenuItemFormProps) {
  const [categoryId, setCategoryId] = useState(initial?.categoryId ?? categories[0]?.id ?? '');
  const [name, setName] = useState(initial?.name ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [price, setPrice] = useState(initial?.price?.toString() ?? '');
  const [vegetarian, setVegetarian] = useState(initial?.vegetarian ?? false);
  const [spicyLevel, setSpicyLevel] = useState(initial?.spicyLevel ?? 0);
  const [available, setAvailable] = useState(initial?.available ?? true);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    onSubmit({
      categoryId,
      name,
      description,
      price: parseFloat(price),
      vegetarian,
      spicyLevel,
      available,
    });
  }

  return (
    <div className="fixed inset-0 z-10 flex items-center justify-center bg-charcoal/40 px-4">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md rounded-md bg-white p-6 shadow-xl"
      >
        <h2 className="font-display text-lg font-semibold text-ink">
          {initial ? 'Edit item' : 'New menu item'}
        </h2>

        <label className="mb-1 mt-4 block text-xs font-medium uppercase tracking-wide text-slate">
          Category
        </label>
        <select
          required
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          className="w-full rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal"
        >
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>

        <label className="mb-1 mt-3 block text-xs font-medium uppercase tracking-wide text-slate">
          Name
        </label>
        <input
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal"
        />

        <label className="mb-1 mt-3 block text-xs font-medium uppercase tracking-wide text-slate">
          Description
        </label>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={2}
          className="w-full rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal"
        />

        <div className="mt-3 flex gap-3">
          <div className="flex-1">
            <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate">
              Price (₹)
            </label>
            <input
              required
              type="number"
              min="0"
              step="1"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              className="w-full rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal"
            />
          </div>
          <div className="flex-1">
            <label className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate">
              Spice level (0-3)
            </label>
            <input
              type="number"
              min="0"
              max="3"
              value={spicyLevel}
              onChange={(e) => setSpicyLevel(Number(e.target.value))}
              className="w-full rounded-sm border border-paper-dim bg-paper px-3 py-2 text-sm outline-none focus:border-teal"
            />
          </div>
        </div>

        <div className="mt-3 flex gap-4">
          <label className="flex items-center gap-2 text-sm text-ink">
            <input type="checkbox" checked={vegetarian} onChange={(e) => setVegetarian(e.target.checked)} />
            Vegetarian
          </label>
          <label className="flex items-center gap-2 text-sm text-ink">
            <input type="checkbox" checked={available} onChange={(e) => setAvailable(e.target.checked)} />
            Available
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-sm border border-paper-dim px-3 py-1.5 text-sm text-ink-soft hover:bg-paper-dim"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting || !categoryId}
            className="rounded-sm bg-marigold px-4 py-1.5 text-sm font-semibold text-charcoal hover:opacity-90 disabled:opacity-50"
          >
            {submitting ? 'Saving…' : 'Save'}
          </button>
        </div>
      </form>
    </div>
  );
}
