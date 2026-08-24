/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0F1B14',
        paper: '#F6F4EC',
        moss: {
          50: '#F1F6F0',
          100: '#DDEBD8',
          200: '#BBD8B2',
          300: '#93C085',
          400: '#6BA55A',
          500: '#4C8A3E',
          600: '#396E2E',
          700: '#2D5825',
          800: '#22421C',
          900: '#1A3315',
        },
        clay: '#C1683C',
      },
      fontFamily: {
        display: ['"Fraunces"', 'serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
    },
  },
  plugins: [],
}
