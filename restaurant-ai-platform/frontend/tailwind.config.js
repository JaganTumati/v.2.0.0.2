/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        paper: '#FAF7F1',
        'paper-dim': '#F1ECE2',
        ink: '#1C1B1A',
        'ink-soft': '#4A4744',
        charcoal: '#1C1B1A',
        'charcoal-raised': '#26241F',
        marigold: '#E8A33D',
        'marigold-deep': '#C4832A',
        teal: '#1F5F5B',
        'teal-soft': '#DCEAE8',
        brick: '#B23A2E',
        'brick-soft': '#F5DEDB',
        slate: '#6B6862',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        ticket: ['"JetBrains Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
};
