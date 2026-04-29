import React from 'react';
import { createRoot } from 'react-dom/client';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import App from './App.jsx';
import './styles.css';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#b6ff00',
    },
    secondary: {
      main: '#8c8c8c',
    },
    background: {
      default: '#111411',
      paper: '#1b1f1a',
    },
  },
  typography: {
    fontFamily: '"Inter", "Segoe UI", Arial, sans-serif',
    h1: {
      fontWeight: 900,
      letterSpacing: '-0.05em',
    },
    h2: {
      fontWeight: 800,
    },
    h5: {
      fontWeight: 800,
    },
    button: {
      fontWeight: 800,
    },
  },
  shape: {
    borderRadius: 2,
  },
});

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <App />
    </ThemeProvider>
  </React.StrictMode>,
);
