import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert,
  AppBar,
  Autocomplete,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Chip,
  Container,
  Divider,
  FormControlLabel,
  Grid,
  IconButton,
  MenuItem,
  Pagination,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import DoneIcon from '@mui/icons-material/Done';
import EditIcon from '@mui/icons-material/Edit';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import AuthPage from './components/AuthPage.jsx';
import { CarCards, CarDetails } from './components/CarCatalog.jsx';

const emptyCar = {
  brand: '',
  model: '',
  pricePerMinute: '',
  licensePlate: '',
  year: '',
  fuelLevel: '',
  imageUrl: '',
  active: true,
  locationId: '',
  featureIds: [],
};

const emptyBooking = {
  userId: '',
  carId: '',
  startDate: '',
  startClock: '10:00',
  minutes: 60,
};

const emptyUser = {
  name: '',
  email: '',
  phone: '',
  driverLicense: '',
};

const emptyLocation = {
  city: 'Минск',
  address: '',
  latitude: '',
  longitude: '',
  capacity: '',
};

const emptyFeature = {
  name: '',
  description: '',
  icon: '',
};

const sections = [
  { key: 'cars', label: 'Автопарк' },
  { key: 'bookings', label: 'Бронирования' },
  { key: 'users', label: 'Клиенты' },
  { key: 'locations', label: 'Адреса выдачи' },
  { key: 'features', label: 'Особенности' },
];

const ADMIN_ACCOUNT = {
  email: 'admin@carsharing.local',
  password: 'admin',
  name: 'Администратор',
};

async function api(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  let response;
  try {
    response = await fetch(path, {
      headers: {
        ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
        ...(options.headers || {}),
      },
      ...options,
    });
  } catch (err) {
    const msg = err?.message || '';
    if (msg.includes('Failed to fetch') || msg.includes('NetworkError')) {
      throw new Error('Нет соединения с сервером. Проверьте интернет и что бэкенд запущен (тот же адрес, что и сайт).');
    }
    throw err;
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(normalizeApiError(text, response.status));
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

function normalizeApiError(text, status) {
  if (!text) {
    return `Не удалось выполнить действие. Код ошибки: ${status}`;
  }

  const lower = text.toLowerCase();
  if (status === 502 || status === 503 || status === 504
      || lower.includes('application failed to respond')
      || lower.includes('bad gateway')
      || lower.includes('service unavailable')
      || lower.includes('gateway time-out')) {
    return 'Сервер не ответил вовремя или перезапускается (часто из‑за нехватки памяти на хостинге). '
      + 'Обновите страницу через минуту или увеличьте лимит RAM в настройках сервиса.';
  }

  try {
    const parsed = JSON.parse(text);
    const message = `${parsed.message || ''} ${parsed.details || ''}`.toLowerCase();
    if (message.includes('lob') || message.includes('photo') || message.includes('фото')) {
      return 'Не удалось загрузить фото. Попробуйте выбрать другое изображение или повторить позже.';
    }
    if (
      message.includes('не удалось удалить клиента')
      || message.includes('невозможно удалить клиента с активными бронированиями')
      || (message.includes('удал') && message.includes('клиент') && message.includes('бронир'))
      || (message.includes('foreign key') && message.includes('booking'))
    ) {
      return 'Ошибка: невозможно удалить клиента с активными бронированиями';
    }
    return parsed.message || parsed.error || 'Не удалось выполнить действие.';
  } catch {
    if (text.toLowerCase().includes('lob')) {
      return 'Не удалось загрузить фото. Попробуйте выбрать другое изображение или повторить позже.';
    }
    return text.length > 180 ? 'Не удалось выполнить действие. Проверьте данные и повторите попытку.' : text;
  }
}

function toParams(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, value);
    }
  });
  return query.toString();
}

function numberOrNull(value) {
  return value === '' || value === null || value === undefined ? null : Number(value);
}

function displayCar(car) {
  return [car.brand, car.model, car.licensePlate].filter(Boolean).join(' ');
}

function displayUser(user) {
  return `${user.name} (${user.email})`;
}

function displayLocation(location) {
  return `${location.city}, ${location.address}`;
}

function formatDate(value) {
  if (!value) {
    return 'не указано';
  }
  return new Date(value).toLocaleString('ru-RU');
}

function getTomorrowDate() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return date.toISOString().slice(0, 10);
}

function buildStartTime(form) {
  if (form.startDate && form.startClock) {
    return `${form.startDate}T${form.startClock}:00`;
  }
  return '';
}

function pageSlice(items, page, size) {
  return items.slice((page - 1) * size, page * size);
}

function readCredentials() {
  const savedCredentials = localStorage.getItem('carsharingCredentials');
  return savedCredentials ? JSON.parse(savedCredentials) : {};
}

function saveClientPassword(email, password) {
  const credentials = readCredentials();
  credentials[email.toLowerCase()] = password;
  localStorage.setItem('carsharingCredentials', JSON.stringify(credentials));
}

export default function App() {
  const [section, setSection] = useState('cars');
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');

  const [cars, setCars] = useState([]);
  const [carPage, setCarPage] = useState({ page: 1, size: 10, totalPages: 1, totalElements: 0 });
  const [carFilters, setCarFilters] = useState({
    email: '',
    feature: '',
    availability: 'available',
    sortBy: 'id',
    sortDirection: 'ASC',
  });
  const [users, setUsers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [locations, setLocations] = useState([]);
  const [features, setFeatures] = useState([]);

  const [carForm, setCarForm] = useState(emptyCar);
  const [bookingForm, setBookingForm] = useState(emptyBooking);
  const [userForm, setUserForm] = useState(emptyUser);
  const [locationForm, setLocationForm] = useState(emptyLocation);
  const [featureForm, setFeatureForm] = useState(emptyFeature);

  const [editingCar, setEditingCar] = useState(null);
  const [editingBooking, setEditingBooking] = useState(null);
  const [editingUser, setEditingUser] = useState(null);
  const [editingLocation, setEditingLocation] = useState(null);
  const [editingFeature, setEditingFeature] = useState(null);
  const [selectedCar, setSelectedCar] = useState(null);
  const [carPhotoFile, setCarPhotoFile] = useState(null);

  const [bookingFilter, setBookingFilter] = useState({ userId: '', carId: '' });
  const [plainPages, setPlainPages] = useState({ bookings: 1, users: 1, locations: 1, features: 1 });
  const [session, setSession] = useState(() => {
    const savedSession = localStorage.getItem('carsharingSession');
    return savedSession ? JSON.parse(savedSession) : null;
  });
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({
    name: '',
    email: '',
    phone: '',
    driverLicense: '',
    password: '',
  });

  const isAdmin = session?.role === 'ADMIN';
  const visibleSections = isAdmin
    ? sections
    : sections
        .filter((item) => ['cars', 'bookings'].includes(item.key))
        .map((item) => item.key === 'bookings' ? { ...item, label: 'БРОНИРОВАНИЯ' } : item);

  const showError = (err) => {
    setError(err.message || 'Неизвестная ошибка');
    setNotice('');
  };

  const showNotice = (message) => {
    setNotice(message);
    setError('');
  };

  const loadCars = async (nextPage = carPage.page) => {
    const query = toParams({
      email: carFilters.email,
      feature: carFilters.feature,
      availableOnly: carFilters.availability === 'available',
      page: nextPage - 1,
      size: carPage.size,
      sortBy: carFilters.sortBy,
      sortDirection: carFilters.sortDirection,
    });
    const data = await api(`/api/cars/search/paged?${query}`);
    setCars(data.content || []);
    setCarPage({
      page: (data.number || 0) + 1,
      size: data.size || carPage.size,
      totalPages: Math.max(data.totalPages || 1, 1),
      totalElements: data.totalElements || 0,
    });
  };

  const loadAll = async () => {
    setLoading(true);
    try {
      // Последовательно, не Promise.all: на слабом Paaс меньше одновременной нагрузки на БД и память.
      setUsers((await api('/api/users')) || []);
      setBookings((await api('/api/bookings')) || []);
      setLocations((await api('/api/locations')) || []);
      setFeatures((await api('/api/features')) || []);
      await loadCars(1);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (session) {
      loadAll();
    }
  }, [session]);

  useEffect(() => {
    if (!visibleSections.some((item) => item.key === section)) {
      setSection('cars');
    }
  }, [section, visibleSections]);

  const bookingRows = useMemo(() => {
    let rows = bookings;
    if (!isAdmin && session?.user?.id) {
      rows = rows.filter((booking) => booking.userId === session.user.id);
      rows = rows.filter((booking) => booking.status === 'ACTIVE');
    }
    if (bookingFilter.userId) {
      rows = rows.filter((booking) => booking.userId === bookingFilter.userId);
    }
    if (bookingFilter.carId) {
      rows = rows.filter((booking) => booking.carId === bookingFilter.carId);
    }
    return rows;
  }, [bookings, bookingFilter, isAdmin, session]);

  const carsByLocation = useMemo(() => {
    const map = new Map();
    cars.forEach((car) => {
      const key = `${car.locationCity || ''}|${car.locationAddress || ''}`;
      map.set(key, [...(map.get(key) || []), car]);
    });
    return map;
  }, [cars]);

  const bookingsByUser = useMemo(() => {
    const map = new Map();
    bookings.forEach((booking) => map.set(booking.userId, [...(map.get(booking.userId) || []), booking]));
    return map;
  }, [bookings]);

  const resetCar = () => {
    setEditingCar(null);
    setCarForm(emptyCar);
    setCarPhotoFile(null);
  };

  const resetBooking = () => {
    setEditingBooking(null);
    setBookingForm(emptyBooking);
  };

  const resetUser = () => {
    setEditingUser(null);
    setUserForm(emptyUser);
  };

  const resetLocation = () => {
    setEditingLocation(null);
    setLocationForm(emptyLocation);
  };

  const resetFeature = () => {
    setEditingFeature(null);
    setFeatureForm(emptyFeature);
  };

  const submitCar = async (event) => {
    event.preventDefault();
    const payload = {
      car: {
        brand: carForm.brand,
        model: carForm.model,
        pricePerMinute: Number(carForm.pricePerMinute),
        licensePlate: carForm.licensePlate,
        year: numberOrNull(carForm.year),
        fuelLevel: numberOrNull(carForm.fuelLevel),
        imageUrl: carForm.imageUrl,
        active: Boolean(carForm.active),
      },
      locationId: carForm.locationId || null,
      featureIds: carForm.featureIds,
    };

    try {
      let savedCar;
      if (editingCar) {
        savedCar = await api(`/api/cars/${editingCar.id}/managed`, { method: 'PUT', body: JSON.stringify(payload) });
        showNotice('Автомобиль обновлен');
      } else {
        savedCar = await api('/api/cars/managed', { method: 'POST', body: JSON.stringify(payload) });
        showNotice('Автомобиль добавлен');
      }
      if (carPhotoFile && savedCar?.id) {
        await uploadCarPhoto(savedCar.id, carPhotoFile);
      }
      resetCar();
      await loadCars(carPage.page);
    } catch (err) {
      showError(err);
    }
  };

  const uploadCarPhoto = async (carId, photoFile) => {
    const formData = new FormData();
    formData.append('photo', photoFile);
    try {
      return await api(`/api/cars/${carId}/photo`, {
        method: 'POST',
        body: formData,
      });
    } catch {
      throw new Error('Не удалось загрузить фото. Попробуйте выбрать другое изображение или повторить позже.');
    }
  };

  const releaseCar = async (car) => {
    try {
      await api(`/api/cars/${car.id}/release`, { method: 'PATCH' });
      showNotice('Автомобиль освобожден');
      await loadCars(carPage.page);
      const data = await api('/api/bookings');
      setBookings(data || []);
    } catch (err) {
      showError(err);
    }
  };

  const editCar = (car) => {
    const location = locations.find(
      (item) => item.city === car.locationCity && item.address === car.locationAddress,
    );
    setEditingCar(car);
    setCarForm({
      brand: car.brand || '',
      model: car.model || '',
      pricePerMinute: car.pricePerMinute ?? '',
      licensePlate: car.licensePlate || '',
      year: car.year ?? '',
      fuelLevel: car.fuelLevel ?? '',
      imageUrl: car.imageUrl || '',
      active: Boolean(car.active),
      locationId: location?.id || '',
      featureIds: features.filter((feature) => car.features?.includes(feature.name)).map((feature) => feature.id),
    });
  };

  const submitBooking = async (event) => {
    event.preventDefault();
    const query = toParams({
      carId: bookingForm.carId,
      minutes: bookingForm.minutes,
      startTime: buildStartTime(bookingForm),
      userId: isAdmin ? bookingForm.userId : session.user.id,
    });
    try {
      if (editingBooking) {
        await api(`/api/bookings/${editingBooking.id}?${query}`, { method: 'PUT' });
        showNotice('Бронирование обновлено');
      } else {
        await api(`/api/bookings?${query}`, { method: 'POST' });
        showNotice('Бронирование создано');
      }
      resetBooking();
      const data = await api('/api/bookings');
      setBookings(data || []);
      await loadCars(carPage.page);
    } catch (err) {
      showError(err);
    }
  };

  const editBooking = (booking) => {
    const minutes = booking.startTime && booking.endTime
      ? Math.max(1, Math.round((new Date(booking.endTime) - new Date(booking.startTime)) / 60000))
      : 60;
    setEditingBooking(booking);
    setBookingForm({
      userId: booking.userId || '',
      carId: booking.carId || '',
      startDate: booking.startTime?.slice(0, 10) || '',
      startClock: booking.startTime?.slice(11, 16) || '10:00',
      minutes,
    });
  };

  const submitSimple = async (event, config) => {
    event.preventDefault();
    try {
      if (config.editing) {
        await api(`${config.path}/${config.editing.id}`, {
          method: 'PUT',
          body: JSON.stringify(config.form),
        });
        showNotice(config.updated);
      } else {
        await api(config.path, {
          method: 'POST',
          body: JSON.stringify(config.form),
        });
        showNotice(config.created);
      }
      config.reset();
      const data = await api(config.path);
      config.setData(data || []);
    } catch (err) {
      showError(err);
    }
  };

  const deleteEntity = async (path, afterDelete, message) => {
    if (!window.confirm('Удалить выбранную запись?')) {
      return;
    }
    try {
      await api(path, { method: 'DELETE' });
      showNotice(message);
      await afterDelete();
    } catch (err) {
      showError(err);
    }
  };

  const completeBooking = async (booking) => {
    try {
      await api(`/api/bookings/${booking.id}/complete`, { method: 'PATCH' });
      showNotice('Бронирование завершено');
      const data = await api('/api/bookings');
      setBookings(data || []);
      await loadCars(carPage.page);
    } catch (err) {
      showError(err);
    }
  };

  const saveSession = (nextSession) => {
    localStorage.setItem('carsharingSession', JSON.stringify(nextSession));
    setSession(nextSession);
    setSection('cars');
  };

  const logout = () => {
    localStorage.removeItem('carsharingSession');
    setSession(null);
    setCars([]);
    setBookings([]);
    setUsers([]);
    setLocations([]);
    setFeatures([]);
  };

  const login = async (event) => {
    event.preventDefault();
    if (authForm.email === ADMIN_ACCOUNT.email && authForm.password === ADMIN_ACCOUNT.password) {
      saveSession({
        role: 'ADMIN',
        user: { name: ADMIN_ACCOUNT.name, email: ADMIN_ACCOUNT.email },
      });
      return;
    }

    try {
      const data = await api('/api/users');
      const user = data.find((item) => item.email.toLowerCase() === authForm.email.toLowerCase());
      if (!user) {
        throw new Error('Клиент с таким email не найден. Зарегистрируйтесь или войдите как администратор.');
      }
      const credentials = readCredentials();
      const savedPassword = credentials[authForm.email.toLowerCase()];
      if (savedPassword && savedPassword !== authForm.password) {
        throw new Error('Неверный пароль.');
      }
      saveSession({ role: 'CLIENT', user });
    } catch (err) {
      showError(err);
    }
  };

  const register = async (event) => {
    event.preventDefault();
    try {
      const user = await api('/api/users', {
        method: 'POST',
        body: JSON.stringify({
          name: authForm.name,
          email: authForm.email,
          phone: authForm.phone,
          driverLicense: authForm.driverLicense,
        }),
      });
      saveClientPassword(authForm.email, authForm.password);
      saveSession({ role: 'CLIENT', user });
    } catch (err) {
      showError(err);
    }
  };

  if (!session) {
    return (
      <AuthPage
        authForm={authForm}
        authMode={authMode}
        error={error}
        onChangeForm={setAuthForm}
        onChangeMode={(mode) => {
          setAuthMode(mode);
          setError('');
        }}
        onLogin={login}
        onRegister={register}
      />
    );
  }

  return (
    <Box>
      <AppBar position="sticky" elevation={0} sx={{ bgcolor: 'rgba(35, 39, 36, 0.96)', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
        <Toolbar sx={{ gap: 3 }}>
          <Stack direction="row" alignItems="center" justifyContent="center" spacing={1.5} sx={{ pr: 3, borderRight: '1px solid rgba(255,255,255,0.1)' }}>
            <Box
              component="img"
              className="wheel-logo"
              src="/brand-logo.png"
              alt=""
              aria-hidden="true"
            />
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 900, lineHeight: 0.9, mt: 0.45 }}>
                Car<span className="accent">sharing</span>
              </Typography>
            </Box>
          </Stack>
          <Stack direction="row" spacing={2} sx={{ flexGrow: 1, overflowX: 'auto' }}>
            {visibleSections.map((item) => (
              <Button
                key={item.key}
                color={section === item.key ? 'primary' : 'inherit'}
                onClick={() => setSection(item.key)}
              >
                {item.label}
              </Button>
            ))}
          </Stack>
          <Stack alignItems="flex-end">
            <Typography variant="body2" fontWeight={800}>
              {session.user.name}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {isAdmin ? 'Администратор' : 'Клиент'}
            </Typography>
          </Stack>
          <Button color="primary" variant="outlined" onClick={logout}>Выйти</Button>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ py: 4 }}>
        {notice && <Alert severity="success" sx={{ mb: 2 }}>{notice}</Alert>}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {section === 'cars' && (
          <CarsSection
            cars={cars}
            carForm={carForm}
            carPage={carPage}
            carFilters={carFilters}
            editingCar={editingCar}
            features={features}
            isAdmin={isAdmin}
            locations={locations}
            selectedCar={selectedCar}
            photoFile={carPhotoFile}
            onChangeForm={setCarForm}
            onChangeFilters={setCarFilters}
            onClearSelectedCar={() => setSelectedCar(null)}
            onEdit={editCar}
            onBookCar={(car) => {
              setBookingForm((form) => ({
                ...form,
                carId: car.id,
                startDate: getTomorrowDate(),
                startClock: '10:00',
              }));
              setSelectedCar(null);
              setSection('bookings');
            }}
            onPhotoFileChange={setCarPhotoFile}
            onReset={resetCar}
            onSelectCar={setSelectedCar}
            onSubmit={submitCar}
            onSearch={() => loadCars(1)}
            onPageChange={(page) => loadCars(page)}
            onRelease={releaseCar}
            onDelete={(car) => deleteEntity(`/api/cars/${car.id}`, () => loadCars(carPage.page), 'Автомобиль удален')}
          />
        )}

        {section === 'bookings' && (
          <BookingsSection
            bookings={bookingRows}
            bookingFilter={bookingFilter}
            bookingForm={bookingForm}
            cars={cars}
            currentUser={session.user}
            editingBooking={editingBooking}
            isAdmin={isAdmin}
            page={plainPages.bookings}
            users={users}
            onChangeFilter={setBookingFilter}
            onChangeForm={setBookingForm}
            onComplete={completeBooking}
            onEdit={editBooking}
            onPage={(page) => setPlainPages((state) => ({ ...state, bookings: page }))}
            onReset={resetBooking}
            onSubmit={submitBooking}
            onDelete={(booking) => deleteEntity(
              `/api/bookings/${booking.id}`,
              async () => setBookings(await api('/api/bookings') || []),
              'Бронирование удалено',
            )}
          />
        )}

        {isAdmin && section === 'users' && (
          <UsersSection
            bookingsByUser={bookingsByUser}
            editingUser={editingUser}
            form={userForm}
            page={plainPages.users}
            users={users}
            onChangeForm={setUserForm}
            onEdit={(user) => {
              setEditingUser(user);
              setUserForm({
                name: user.name || '',
                email: user.email || '',
                phone: user.phone || '',
                driverLicense: user.driverLicense || '',
              });
            }}
            onPage={(page) => setPlainPages((state) => ({ ...state, users: page }))}
            onReset={resetUser}
            onSubmit={(event) => submitSimple(event, {
              created: 'Клиент создан',
              editing: editingUser,
              form: userForm,
              path: '/api/users',
              reset: resetUser,
              setData: setUsers,
              updated: 'Клиент обновлен',
            })}
            onDelete={(user) => deleteEntity(
              `/api/users/${user.id}`,
              async () => setUsers(await api('/api/users') || []),
              'Клиент удален',
            )}
          />
        )}

        {isAdmin && section === 'locations' && (
          <LocationsSection
            carsByLocation={carsByLocation}
            editingLocation={editingLocation}
            form={locationForm}
            locations={locations}
            page={plainPages.locations}
            onChangeForm={setLocationForm}
            onEdit={(location) => {
              setEditingLocation(location);
              setLocationForm({
                city: location.city || '',
                address: location.address || '',
                latitude: location.latitude ?? '',
                longitude: location.longitude ?? '',
                capacity: location.capacity ?? '',
              });
            }}
            onPage={(page) => setPlainPages((state) => ({ ...state, locations: page }))}
            onReset={resetLocation}
            onSubmit={(event) => submitSimple(event, {
              created: 'Локация создана',
              editing: editingLocation,
              form: {
                ...locationForm,
                latitude: numberOrNull(locationForm.latitude),
                longitude: numberOrNull(locationForm.longitude),
                capacity: numberOrNull(locationForm.capacity),
              },
              path: '/api/locations',
              reset: resetLocation,
              setData: setLocations,
              updated: 'Локация обновлена',
            })}
            onDelete={(location) => deleteEntity(
              `/api/locations/${location.id}`,
              async () => setLocations(await api('/api/locations') || []),
              'Локация удалена',
            )}
          />
        )}

        {isAdmin && section === 'features' && (
          <FeaturesSection
            cars={cars}
            editingFeature={editingFeature}
            features={features}
            form={featureForm}
            page={plainPages.features}
            onChangeForm={setFeatureForm}
            onEdit={(feature) => {
              setEditingFeature(feature);
              setFeatureForm({
                name: feature.name || '',
                description: feature.description || '',
                icon: feature.icon || '',
              });
            }}
            onPage={(page) => setPlainPages((state) => ({ ...state, features: page }))}
            onReset={resetFeature}
            onSubmit={(event) => submitSimple(event, {
              created: 'Особенность создана',
              editing: editingFeature,
              form: featureForm,
              path: '/api/features',
              reset: resetFeature,
              setData: setFeatures,
              updated: 'Особенность обновлена',
            })}
            onDelete={(feature) => deleteEntity(
              `/api/features/${feature.id}`,
              async () => setFeatures(await api('/api/features') || []),
              'Особенность удалена',
            )}
          />
        )}
      </Container>
    </Box>
  );
}

function SectionShell({ title, subtitle, form, table }) {
  return (
    <Grid container spacing={3}>
      {form && (
        <Grid size={{ xs: 12, md: 4 }}>
          <Card className="muted-card">
            <CardContent>
              <Typography component="div" variant="h5">{title}</Typography>
              <Typography color="text.secondary" sx={{ mb: 2 }}>{subtitle}</Typography>
              {form}
            </CardContent>
          </Card>
        </Grid>
      )}
      <Grid size={{ xs: 12, md: form ? 8 : 12 }}>
        {!form && (
          <Box sx={{ mb: 2 }}>
            <Typography component="div" variant="h5">{title}</Typography>
            <Typography color="text.secondary">{subtitle}</Typography>
          </Box>
        )}
        {table}
      </Grid>
    </Grid>
  );
}

function CarsSection(props) {
  const {
    cars,
    carFilters,
    carForm,
    carPage,
    editingCar,
    features,
    isAdmin,
    locations,
    photoFile,
    selectedCar,
    onChangeFilters,
    onChangeForm,
    onClearSelectedCar,
    onDelete,
    onEdit,
    onBookCar,
    onPhotoFileChange,
    onPageChange,
    onRelease,
    onReset,
    onSelectCar,
    onSearch,
    onSubmit,
  } = props;
  const { formRef: editFormRef, highlightForm: highlightEditForm } = useEditFormFocus(isAdmin && Boolean(editingCar));

  if (!isAdmin && selectedCar) {
    return (
      <CarDetails
        car={selectedCar}
        onBack={onClearSelectedCar}
        onBookCar={onBookCar}
      />
    );
  }

  return (
    <SectionShell
      title={isAdmin ? (editingCar ? 'Редактировать автомобиль' : 'Новый автомобиль') : (
        <Stack direction="row" spacing={1} alignItems="center">
          <span>Выберите автомобиль</span>
          <Tooltip title='Клиент видит доступные машины и может оформить бронирование в разделе "Бронирования".'>
            <Box component="span" className="help-dot">?</Box>
          </Tooltip>
        </Stack>
      )}
      subtitle={isAdmin
        ? 'Локация и особенности выбираются из справочников.'
        : ''}
      form={isAdmin ? (
        <Box
          ref={editFormRef}
          component="form"
          onSubmit={onSubmit}
          sx={{
            scrollMarginTop: '90px',
            borderRadius: 1,
            transition: 'box-shadow 220ms ease, background-color 220ms ease',
            boxShadow: highlightEditForm ? '0 0 0 2px rgba(182, 255, 0, 0.65)' : 'none',
            backgroundColor: highlightEditForm ? 'rgba(182, 255, 0, 0.06)' : 'transparent',
          }}
        >
          <Stack spacing={2}>
            <TextField label="Марка" required value={carForm.brand}
              onChange={(event) => onChangeForm({ ...carForm, brand: event.target.value })} />
            <TextField label="Модель" required value={carForm.model}
              onChange={(event) => onChangeForm({ ...carForm, model: event.target.value })} />
            <TextField label="Цена за минуту" required type="number" slotProps={{ htmlInput: { step: 0.1 } }}
              value={carForm.pricePerMinute}
              onChange={(event) => onChangeForm({ ...carForm, pricePerMinute: event.target.value })} />
            <TextField label="Номер" required value={carForm.licensePlate}
              onChange={(event) => onChangeForm({ ...carForm, licensePlate: event.target.value })} />
            <TextField label="Год" type="number" value={carForm.year}
              onChange={(event) => onChangeForm({ ...carForm, year: event.target.value })} />
            <TextField label="Топливо, %" type="number" value={carForm.fuelLevel}
              onChange={(event) => onChangeForm({ ...carForm, fuelLevel: event.target.value })} />
            <TextField label="Фото автомобиля (URL)" value={carForm.imageUrl}
              onChange={(event) => onChangeForm({ ...carForm, imageUrl: event.target.value })} />
            <Button variant="outlined" component="label">
              Загрузить фото
              <input
                hidden
                type="file"
                accept="image/*"
                onChange={async (event) => {
                  const file = event.target.files?.[0];
                  if (file) {
                    onPhotoFileChange(file);
                  }
                }}
              />
            </Button>
            {photoFile && (
              <Typography color="text.secondary" variant="body2">
                Выбрано фото: {photoFile.name}
              </Typography>
            )}
            <TextField select label="Локация" value={carForm.locationId}
              onChange={(event) => onChangeForm({ ...carForm, locationId: event.target.value })}>
              <MenuItem value="">Без локации</MenuItem>
              {locations.map((location) => (
                <MenuItem key={location.id} value={location.id}>{displayLocation(location)}</MenuItem>
              ))}
            </TextField>
            <Autocomplete
              multiple
              options={features}
              getOptionLabel={(feature) => feature.name}
              value={features.filter((feature) => carForm.featureIds.includes(feature.id))}
              onChange={(_, value) => onChangeForm({ ...carForm, featureIds: value.map((feature) => feature.id) })}
              renderInput={(params) => <TextField {...params} label="Особенности" />}
            />
            <FormControlLabel control={<Checkbox checked={carForm.active}
              onChange={(event) => onChangeForm({ ...carForm, active: event.target.checked })} />}
              label="Активна" />
            <Button type="submit" variant="contained">{editingCar ? 'Сохранить' : 'Добавить'}</Button>
            {editingCar && <Button onClick={onReset}>Отменить редактирование</Button>}
          </Stack>
        </Box>
      ) : null}
      table={(
        <Stack spacing={2}>
          <Paper sx={{ p: 2 }}>
            <Grid container spacing={2}>
              {isAdmin && (
                <Grid size={{ xs: 12, md: 3 }}>
                  <TextField fullWidth label="Фильтр по email клиента" value={carFilters.email}
                    onChange={(event) => onChangeFilters({ ...carFilters, email: event.target.value })} />
                </Grid>
              )}
              <Grid size={{ xs: 12, md: isAdmin ? 3 : 3 }}>
                <TextField select fullWidth label="Фильтр по особенности" value={carFilters.feature}
                  onChange={(event) => onChangeFilters({ ...carFilters, feature: event.target.value })}>
                  <MenuItem value="">Все особенности</MenuItem>
                  {features.map((feature) => <MenuItem key={feature.id} value={feature.name}>{feature.name}</MenuItem>)}
                </TextField>
              </Grid>
              {!isAdmin && (
                <Grid size={{ xs: 12, md: 3 }}>
                  <TextField select fullWidth label="Показать" value={carFilters.availability}
                    onChange={(event) => onChangeFilters({ ...carFilters, availability: event.target.value })}>
                    <MenuItem value="available">Свободные</MenuItem>
                    <MenuItem value="all">Все</MenuItem>
                  </TextField>
                </Grid>
              )}
              <Grid size={{ xs: 6, md: isAdmin ? 3 : 3 }}>
                <TextField select fullWidth label="Сортировка" value={carFilters.sortBy}
                  onChange={(event) => onChangeFilters({ ...carFilters, sortBy: event.target.value })}>
                  <MenuItem value="id">По добавлению</MenuItem>
                  <MenuItem value="brand">Марка</MenuItem>
                  <MenuItem value="pricePerMinute">Цена</MenuItem>
                  <MenuItem value="year">Год</MenuItem>
                </TextField>
              </Grid>
              <Grid size={{ xs: 6, md: isAdmin ? 3 : 3 }}>
                <Button fullWidth sx={{ height: '100%' }} variant="outlined" onClick={onSearch}>Найти</Button>
              </Grid>
            </Grid>
          </Paper>
          {isAdmin ? (
            <TableCard>
              <Table sx={{ tableLayout: 'fixed', width: '100%' }}>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ width: '21%' }}>Автомобиль</TableCell>
                    <TableCell sx={{ width: '21%' }}>Локация</TableCell>
                    <TableCell sx={{ width: '20%' }}>Особенности</TableCell>
                    <TableCell sx={{ width: '10%' }}>Цена</TableCell>
                    <TableCell sx={{ width: '14%' }}>Статус</TableCell>
                    <TableCell align="right">Действия</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cars.map((car) => (
                    <TableRow key={car.id} hover>
                      <TableCell>
                        <Typography fontWeight={700} sx={{ fontSize: '0.9rem' }}>{displayCar(car)}</Typography>
                        <Typography color="text.secondary" sx={{ fontSize: '0.95rem' }}>
                          {car.year || 'год не указан'} · топливо {car.fuelLevel ?? 0}%
                        </Typography>
                      </TableCell>
                      <TableCell sx={{ overflowWrap: 'anywhere' }}>
                        {car.locationCity ? `${car.locationCity}, ${car.locationAddress}` : 'не назначена'}
                      </TableCell>
                      <TableCell sx={{ overflow: 'hidden' }}>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, maxWidth: '100%' }}>
                          {(car.features || []).map((feature) => (
                            <Chip
                              key={feature}
                              size="small"
                              label={feature}
                              sx={{
                                maxWidth: '100%',
                                flexShrink: 0,
                                '& .MuiChip-label': {
                                  whiteSpace: 'normal',
                                  wordBreak: 'keep-all',
                                  overflowWrap: 'normal',
                                  lineHeight: 1.2,
                                  paddingTop: '3px',
                                  paddingBottom: '3px',
                                },
                              }}
                            />
                          ))}
                        </Box>
                      </TableCell>
                      <TableCell sx={{ overflowWrap: 'anywhere' }}>{car.pricePerMinute} BYN/мин</TableCell>
                      <TableCell>
                        <Chip color={car.available ? 'primary' : 'default'} label={car.available ? 'Available' : 'Busy'} />
                      </TableCell>
                      <TableCell align="right">
                        <RowActions
                          onRelease={!car.available ? () => onRelease(car) : null}
                          onEdit={() => onEdit(car)}
                          onDelete={() => onDelete(car)}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableCard>
          ) : (
            <CarCards cars={cars} onSelectCar={onSelectCar} />
          )}
          <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography color="text.secondary">
              Найдено автомобилей: {carPage.totalElements}. Страница {carPage.page} из {carPage.totalPages}.
            </Typography>
            <Pagination
              page={carPage.page}
              count={carPage.totalPages}
              showFirstButton
              showLastButton
              onChange={(_, page) => onPageChange(page)}
            />
          </Stack>
        </Stack>
      )}
    />
  );
}

function BookingsSection(props) {
  const {
    bookingFilter,
    bookingForm,
    bookings,
    cars,
    currentUser,
    editingBooking,
    isAdmin,
    onChangeFilter,
    onChangeForm,
    onComplete,
    onDelete,
    onEdit,
    onPage,
    onReset,
    onSubmit,
    page,
    users,
  } = props;
  const rows = pageSlice(bookings, page, 6);
  const { formRef: editFormRef, highlightForm: highlightEditForm } = useEditFormFocus(Boolean(editingBooking));

  return (
    <SectionShell
      title={editingBooking ? 'Редактировать бронирование' : 'Новое бронирование'}
      subtitle=""
      form={(
        <Box
          ref={editFormRef}
          component="form"
          onSubmit={onSubmit}
          sx={getEditFormSx(highlightEditForm)}
        >
          <Stack spacing={2}>
            {isAdmin ? (
              <Autocomplete options={users} getOptionLabel={displayUser}
                value={users.find((user) => user.id === bookingForm.userId) || null}
                onChange={(_, value) => onChangeForm({ ...bookingForm, userId: value?.id || '' })}
                renderInput={(params) => <TextField {...params} required label="Клиент" />} />
            ) : (
              <TextField label="Клиент" value={displayUser(currentUser)} disabled />
            )}
            <Autocomplete options={cars} getOptionLabel={displayCar}
              value={cars.find((car) => car.id === bookingForm.carId) || null}
              onChange={(_, value) => onChangeForm({ ...bookingForm, carId: value?.id || '' })}
              renderInput={(params) => <TextField {...params} required label="Автомобиль" />} />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" color="text.secondary">Дата начала</Typography>
                <TextField fullWidth required type="date"
                  value={bookingForm.startDate}
                  onChange={(event) => onChangeForm({ ...bookingForm, startDate: event.target.value })} />
              </Box>
              <Box sx={{ width: { xs: '100%', sm: 150 } }}>
                <Typography variant="caption" color="text.secondary">Время начала</Typography>
                <TextField fullWidth required type="time"
                  value={bookingForm.startClock}
                  onChange={(event) => onChangeForm({ ...bookingForm, startClock: event.target.value })} />
              </Box>
            </Stack>
            <TextField label="Минуты" required type="number" value={bookingForm.minutes}
              onChange={(event) => onChangeForm({ ...bookingForm, minutes: event.target.value })} />
            <Button type="submit" variant="contained">{editingBooking ? 'Сохранить' : 'Забронировать'}</Button>
            {editingBooking && <Button onClick={onReset}>Отменить редактирование</Button>}
          </Stack>
        </Box>
      )}
      table={(
        <Stack spacing={2}>
          {isAdmin && (
            <Paper sx={{ p: 2 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Autocomplete options={users} getOptionLabel={displayUser}
                    value={users.find((user) => user.id === bookingFilter.userId) || null}
                    onChange={(_, value) => onChangeFilter({ ...bookingFilter, userId: value?.id || '' })}
                    renderInput={(params) => <TextField {...params} label="Фильтр по клиенту" />} />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Autocomplete options={cars} getOptionLabel={displayCar}
                    value={cars.find((car) => car.id === bookingFilter.carId) || null}
                    onChange={(_, value) => onChangeFilter({ ...bookingFilter, carId: value?.id || '' })}
                    renderInput={(params) => <TextField {...params} label="Фильтр по автомобилю" />} />
                </Grid>
              </Grid>
            </Paper>
          )}
          <TableCard>
            <Table>
              <TableHead>
                <TableRow>
                  {isAdmin && <TableCell>Клиент</TableCell>}
                  <TableCell>Автомобиль</TableCell>
                  <TableCell>Период</TableCell>
                  <TableCell>Стоимость</TableCell>
                  <TableCell sx={{ width: 120 }}>Статус</TableCell>
                  <TableCell align="right">Действия</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.map((booking) => (
                  <TableRow key={booking.id} hover>
                    {isAdmin && <TableCell>{booking.userName}</TableCell>}
                    <TableCell>{booking.carBrand} {booking.carModel}</TableCell>
                    <TableCell>{formatDate(booking.startTime)} - {formatDate(booking.endTime)}</TableCell>
                    <TableCell>{booking.totalCost ?? 0} BYN</TableCell>
                    <TableCell sx={{ width: 120 }}>
                      <Chip label={booking.status} color={booking.status === 'ACTIVE' ? 'primary' : 'default'} />
                    </TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.75} sx={{ justifyContent: 'flex-end', alignItems: 'center' }}>
                        {isAdmin && (
                          <Tooltip title="Завершить">
                            <span>
                              <IconButton
                                className="edit-action"
                                disabled={booking.status !== 'ACTIVE'}
                                onClick={() => onComplete(booking)}
                              >
                                <DoneIcon />
                              </IconButton>
                            </span>
                          </Tooltip>
                        )}
                        <RowActions onEdit={() => onEdit(booking)} onDelete={() => onDelete(booking)} />
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableCard>
          <FriendlyPager
            page={page}
            totalPages={Math.max(Math.ceil(bookings.length / 6), 1)}
            onPage={onPage}
          />
        </Stack>
      )}
    />
  );
}

function UsersSection({ bookingsByUser, editingUser, form, onChangeForm, onDelete, onEdit, onPage, onReset, onSubmit, page, users }) {
  const rows = pageSlice(users, page, 6);
  const { formRef: editFormRef, highlightForm: highlightEditForm } = useEditFormFocus(Boolean(editingUser));
  return (
    <SectionShell
      title={editingUser ? 'Редактировать клиента' : 'Новый клиент'}
      subtitle="Добавление нового клиента."
      form={(
        <Box ref={editFormRef} sx={getEditFormSx(highlightEditForm)}>
          <SimpleUserForm form={form} editing={editingUser} onChange={onChangeForm} onReset={onReset} onSubmit={onSubmit} />
        </Box>
      )}
      table={(
        <PagedTable page={page} total={users.length} onPage={onPage}>
          <Table>
            <TableHead><TableRow><TableCell>Клиент</TableCell><TableCell>Контакты</TableCell><TableCell>Бронирования</TableCell><TableCell align="right">Действия</TableCell></TableRow></TableHead>
            <TableBody>
              {rows.map((user) => (
                <TableRow key={user.id} hover>
                  <TableCell><Typography fontWeight={800}>{user.name}</Typography><Typography color="text.secondary">{user.driverLicense}</Typography></TableCell>
                  <TableCell>{user.email}<br />{user.phone}</TableCell>
                  <TableCell>{(bookingsByUser.get(user.id) || []).map((booking) => `${booking.carBrand} ${booking.carModel}`).join(', ') || 'нет бронирований'}</TableCell>
                  <TableCell align="right"><RowActions onEdit={() => onEdit(user)} onDelete={() => onDelete(user)} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </PagedTable>
      )}
    />
  );
}

function LocationsSection({ carsByLocation, editingLocation, form, locations, onChangeForm, onDelete, onEdit, onPage, onReset, onSubmit, page }) {
  const rows = pageSlice(locations, page, 6);
  const { formRef: editFormRef, highlightForm: highlightEditForm } = useEditFormFocus(Boolean(editingLocation));
  return (
    <SectionShell
      title={editingLocation ? 'Редактировать локацию' : 'Новая локация'}
      subtitle="Адреса выдачи автомобилей в Минске."
      form={(
        <Box ref={editFormRef} sx={getEditFormSx(highlightEditForm)}>
          <LocationForm form={form} editing={editingLocation} onChange={onChangeForm} onReset={onReset} onSubmit={onSubmit} />
        </Box>
      )}
      table={(
        <PagedTable page={page} total={locations.length} onPage={onPage}>
          <Table>
            <TableHead><TableRow><TableCell>Локация</TableCell><TableCell>Координаты</TableCell><TableCell>Автомобили</TableCell><TableCell align="right">Действия</TableCell></TableRow></TableHead>
            <TableBody>
              {rows.map((location) => {
                const key = `${location.city}|${location.address}`;
                return (
                  <TableRow key={location.id} hover>
                    <TableCell><Typography fontWeight={800}>{displayLocation(location)}</Typography><Typography color="text.secondary">Вместимость: {location.capacity || 'не указана'}</Typography></TableCell>
                    <TableCell>{location.latitude || '-'}, {location.longitude || '-'}</TableCell>
                    <TableCell>{(carsByLocation.get(key) || []).map(displayCar).join(', ') || 'нет автомобилей'}</TableCell>
                    <TableCell align="right"><RowActions onEdit={() => onEdit(location)} onDelete={() => onDelete(location)} /></TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </PagedTable>
      )}
    />
  );
}

function FeaturesSection({ cars, editingFeature, features, form, onChangeForm, onDelete, onEdit, onPage, onReset, onSubmit, page }) {
  const rows = pageSlice(features, page, 6);
  const { formRef: editFormRef, highlightForm: highlightEditForm } = useEditFormFocus(Boolean(editingFeature));
  return (
    <SectionShell
      title={editingFeature ? 'Редактировать особенность' : 'Новая особенность'}
      subtitle="Добавление новой особенности."
      form={(
        <Box ref={editFormRef} sx={getEditFormSx(highlightEditForm)}>
          <FeatureForm form={form} editing={editingFeature} onChange={onChangeForm} onReset={onReset} onSubmit={onSubmit} />
        </Box>
      )}
      table={(
        <PagedTable page={page} total={features.length} onPage={onPage}>
          <Table>
            <TableHead><TableRow><TableCell>Особенность</TableCell><TableCell>Описание</TableCell><TableCell>Автомобили</TableCell><TableCell align="right">Действия</TableCell></TableRow></TableHead>
            <TableBody>
              {rows.map((feature) => (
                <TableRow key={feature.id} hover>
                  <TableCell><Typography fontWeight={800}>{feature.name}</Typography><Typography color="text.secondary">{feature.icon}</Typography></TableCell>
                  <TableCell>{feature.description}</TableCell>
                  <TableCell>{cars.filter((car) => car.features?.includes(feature.name)).map(displayCar).join(', ') || 'не используется'}</TableCell>
                  <TableCell align="right"><RowActions onEdit={() => onEdit(feature)} onDelete={() => onDelete(feature)} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </PagedTable>
      )}
    />
  );
}

function SimpleUserForm({ editing, form, onChange, onReset, onSubmit }) {
  return (
    <Box component="form" onSubmit={onSubmit}>
      <Stack spacing={2}>
        <TextField label="Имя" required value={form.name} onChange={(event) => onChange({ ...form, name: event.target.value })} />
        <TextField label="Email" required type="email" value={form.email} onChange={(event) => onChange({ ...form, email: event.target.value })} />
        <TextField label="Телефон" value={form.phone} onChange={(event) => onChange({ ...form, phone: event.target.value })} />
        <TextField label="Водительское удостоверение" value={form.driverLicense} onChange={(event) => onChange({ ...form, driverLicense: event.target.value })} />
        <Button type="submit" variant="contained">{editing ? 'Сохранить' : 'Добавить'}</Button>
        {editing && <Button onClick={onReset}>Отменить редактирование</Button>}
      </Stack>
    </Box>
  );
}

function LocationForm({ editing, form, onChange, onReset, onSubmit }) {
  return (
    <Box component="form" onSubmit={onSubmit}>
      <Stack spacing={2}>
        <TextField label="Город" required value={form.city} onChange={(event) => onChange({ ...form, city: event.target.value })} />
        <TextField label="Адрес" required value={form.address} onChange={(event) => onChange({ ...form, address: event.target.value })} />
        <TextField label="Широта" type="number" value={form.latitude} onChange={(event) => onChange({ ...form, latitude: event.target.value })} />
        <TextField label="Долгота" type="number" value={form.longitude} onChange={(event) => onChange({ ...form, longitude: event.target.value })} />
        <TextField label="Вместимость" type="number" value={form.capacity} onChange={(event) => onChange({ ...form, capacity: event.target.value })} />
        <Button type="submit" variant="contained">{editing ? 'Сохранить' : 'Добавить'}</Button>
        {editing && <Button onClick={onReset}>Отменить редактирование</Button>}
      </Stack>
    </Box>
  );
}

function FeatureForm({ editing, form, onChange, onReset, onSubmit }) {
  return (
    <Box component="form" onSubmit={onSubmit}>
      <Stack spacing={2}>
        <TextField label="Название" required value={form.name} onChange={(event) => onChange({ ...form, name: event.target.value })} />
        <TextField label="Описание" value={form.description} onChange={(event) => onChange({ ...form, description: event.target.value })} />
        <TextField label="Иконка" value={form.icon} onChange={(event) => onChange({ ...form, icon: event.target.value })} />
        <Button type="submit" variant="contained">{editing ? 'Сохранить' : 'Добавить'}</Button>
        {editing && <Button onClick={onReset}>Отменить редактирование</Button>}
      </Stack>
    </Box>
  );
}

function TableCard({ children }) {
  return (
    <TableContainer
      component={Paper}
      sx={{
        overflowX: 'hidden',
        '& .MuiTableCell-root': {
          fontSize: '0.9rem',
          lineHeight: 1.3,
          py: 1.2,
        },
        '& .MuiChip-root': {
          fontSize: '0.82rem',
        },
      }}
    >
      {children}
    </TableContainer>
  );
}

function PagedTable({ children, onPage, page, total }) {
  const totalPages = Math.max(Math.ceil(total / 6), 1);

  return (
    <Stack spacing={2}>
      <TableCard>{children}</TableCard>
      <Divider />
      <FriendlyPager page={page} totalPages={totalPages} onPage={onPage} />
    </Stack>
  );
}

function FriendlyPager({ onPage, page, totalPages }) {
  return (
    <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
      <Typography color="text.secondary">Страница {page} из {totalPages}</Typography>
      <Pagination
        page={page}
        count={totalPages}
        showFirstButton
        showLastButton
        onChange={(_, value) => onPage(value)}
      />
    </Stack>
  );
}

function RowActions({ onDelete, onEdit, onRelease }) {
  return (
    <Stack className="row-actions" direction="row" spacing={0.75}>
      {onRelease && (
        <Tooltip title="Освободить">
          <IconButton className="release-action" onClick={onRelease}><LockOpenIcon /></IconButton>
        </Tooltip>
      )}
      <Tooltip title="Редактировать">
        <IconButton className="edit-action" onClick={onEdit}><EditIcon /></IconButton>
      </Tooltip>
      <Tooltip title="Удалить">
        <IconButton className="delete-action" onClick={onDelete}><DeleteIcon /></IconButton>
      </Tooltip>
    </Stack>
  );
}

function useEditFormFocus(isEditing) {
  const formRef = useRef(null);
  const [highlightForm, setHighlightForm] = useState(false);

  useEffect(() => {
    if (!isEditing || !formRef.current) {
      return undefined;
    }

    formRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
    setHighlightForm(true);
    const timer = setTimeout(() => setHighlightForm(false), 1400);
    return () => clearTimeout(timer);
  }, [isEditing]);

  return { formRef, highlightForm };
}

function getEditFormSx(highlight) {
  return {
    scrollMarginTop: '90px',
    borderRadius: 1,
    transition: 'box-shadow 220ms ease, background-color 220ms ease',
    boxShadow: highlight ? '0 0 0 2px rgba(182, 255, 0, 0.65)' : 'none',
    backgroundColor: highlight ? 'rgba(182, 255, 0, 0.06)' : 'transparent',
  };
}
