import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { AppShell } from './components/AppShell';
import { ProtectedRoute } from './components/ProtectedRoute';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { VolunteerLayout } from './pages/volunteer/VolunteerLayout';
import { VolunteerHomePage } from './pages/volunteer/VolunteerHomePage';
import { VolunteerTasksPage } from './pages/volunteer/VolunteerTasksPage';
import { VolunteerTaskDetailPage } from './pages/volunteer/VolunteerTaskDetailPage';
import { VolunteerProfileSkillsPage } from './pages/volunteer/VolunteerProfileSkillsPage';
import { VolunteerApplicationsPage } from './pages/volunteer/VolunteerApplicationsPage';
import { OrganizerDashboardPage } from './pages/OrganizerDashboardPage';
import { OrganizerApplicationsPage } from './pages/OrganizerApplicationsPage';
import { OrganizerSelfProfilePage } from './pages/OrganizerSelfProfilePage';
import { OrganizerPublicProfilePage } from './pages/OrganizerPublicProfilePage';
import { MatchingPage } from './pages/MatchingPage';
import { CoordinatorDashboardPage } from './pages/CoordinatorDashboardPage';
import { VolunteerProfilePage } from './pages/VolunteerProfilePage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<HomePage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route
              path="volunteer"
              element={
                <ProtectedRoute roles={['VOLUNTEER']}>
                  <VolunteerLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<VolunteerHomePage />} />
              <Route path="tasks" element={<VolunteerTasksPage />} />
              <Route path="tasks/:taskId" element={<VolunteerTaskDetailPage />} />
              <Route path="profile" element={<VolunteerProfileSkillsPage />} />
              <Route path="applications" element={<VolunteerApplicationsPage />} />
            </Route>
            <Route
              path="organizer"
              element={
                <ProtectedRoute roles={['ORGANIZER']}>
                  <OrganizerDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="organizer/applications"
              element={
                <ProtectedRoute roles={['ORGANIZER']}>
                  <OrganizerApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="organizer/profile"
              element={
                <ProtectedRoute roles={['ORGANIZER']}>
                  <OrganizerSelfProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="coordinator"
              element={
                <ProtectedRoute roles={['COORDINATOR', 'ADMIN']}>
                  <CoordinatorDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="matching"
              element={
                <ProtectedRoute roles={['ORGANIZER', 'COORDINATOR', 'ADMIN']}>
                  <MatchingPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="volunteers/:id"
              element={
                <ProtectedRoute roles={['ORGANIZER', 'COORDINATOR', 'ADMIN']}>
                  <VolunteerProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="organizers/:userId"
              element={
                <ProtectedRoute roles={['VOLUNTEER', 'ORGANIZER', 'COORDINATOR', 'ADMIN']}>
                  <OrganizerPublicProfilePage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
