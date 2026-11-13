import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import KpiCards from './components/KpiCards';
import FilterBar from './components/FilterBar';
import TicketTable from './components/TicketTable';
import TicketModal from './components/TicketModal';
import SimulateModal from './components/SimulateModal';

const PRESET_CREDENTIALS = {
  ROLE_ADMIN: { email: 'admin@supportdesk.io', password: 'Admin@123', label: 'Admin (Akash)' },
  ROLE_AGENT: { email: 'agent.sarah@supportdesk.io', password: 'Agent@123', label: 'Support Agent (Sarah)' },
  ROLE_CUSTOMER: { email: 'david@acmecorp.com', password: 'Customer@123', label: 'Customer (David)' }
};

export default function App() {
  const [role, setRole] = useState(() => localStorage.getItem('supportdesk_role') || 'ROLE_ADMIN');
  const [token, setToken] = useState(() => localStorage.getItem('supportdesk_jwt') || null);
  const [currentUser, setCurrentUser] = useState(null);

  const [metrics, setMetrics] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [selectedTicketId, setSelectedTicketId] = useState(null);
  const [ticketDetail, setTicketDetail] = useState(null);
  const [isSimulateOpen, setIsSimulateOpen] = useState(false);

  const [filters, setFilters] = useState({
    handlingType: '',
    status: '',
    urgency: '',
    category: '',
    search: ''
  });

  // Authenticated fetch wrapper that automatically attaches the JWT Bearer token
  const authFetch = useCallback(async (url, options = {}) => {
    const currentToken = localStorage.getItem('supportdesk_jwt');
    const headers = {
      ...(options.headers || {}),
      ...(currentToken ? { Authorization: `Bearer ${currentToken}` } : {})
    };
    return fetch(url, { ...options, headers });
  }, []);

  // Login handler to obtain JWT token for the selected role
  const loginAsRole = useCallback(async (targetRole) => {
    const creds = PRESET_CREDENTIALS[targetRole] || PRESET_CREDENTIALS.ROLE_ADMIN;
    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: creds.email, password: creds.password })
      });

      if (res.ok) {
        const data = await res.json();
        setToken(data.token);
        setCurrentUser(data);
        localStorage.setItem('supportdesk_jwt', data.token);
        localStorage.setItem('supportdesk_role', targetRole);
        return data.token;
      } else {
        console.warn('Auto-login failed for role:', targetRole);
      }
    } catch (err) {
      console.error('Login error:', err);
    }
    return null;
  }, []);

  // Ensure user is authenticated on initial load
  useEffect(() => {
    loginAsRole(role);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const handleRoleChange = async (newRole) => {
    setRole(newRole);
    const newToken = await loginAsRole(newRole);
    if (newToken) {
      refreshAll();
    }
  };

  const fetchMetrics = useCallback(async () => {
    try {
      const res = await authFetch('/api/v1/dashboard/metrics');
      if (res.ok) {
        const data = await res.json();
        setMetrics(data);
      }
    } catch (err) {
      console.error('Failed to fetch metrics:', err);
    }
  }, [authFetch]);

  const fetchTickets = useCallback(async () => {
    try {
      let url = `/api/v1/tickets?size=50`;
      if (filters.status) url += `&status=${filters.status}`;
      if (filters.urgency) url += `&urgency=${filters.urgency}`;
      if (filters.category) url += `&category=${filters.category}`;
      if (filters.handlingType) url += `&handlingType=${filters.handlingType}`;
      if (filters.search) url += `&search=${encodeURIComponent(filters.search)}`;

      const res = await authFetch(url);
      if (res.ok) {
        const page = await res.json();
        setTickets(page.content || []);
      }
    } catch (err) {
      console.error('Failed to fetch tickets:', err);
    }
  }, [filters, authFetch]);

  useEffect(() => {
    fetchMetrics();
    fetchTickets();
  }, [fetchMetrics, fetchTickets]);

  const handleSelectTicket = async (id) => {
    try {
      setSelectedTicketId(id);
      const res = await authFetch(`/api/v1/tickets/${id}`);
      if (res.ok) {
        const detail = await res.json();
        setTicketDetail(detail);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const handleResetFilters = () => {
    setFilters({
      handlingType: '',
      status: '',
      urgency: '',
      category: '',
      search: ''
    });
  };

  const refreshAll = () => {
    fetchMetrics();
    fetchTickets();
    if (selectedTicketId) {
      handleSelectTicket(selectedTicketId);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar
        onOpenSimulate={() => setIsSimulateOpen(true)}
        role={role}
        currentUser={currentUser}
        onRoleChange={handleRoleChange}
      />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full space-y-6">
        <KpiCards metrics={metrics} />

        <FilterBar
          filters={filters}
          onFilterChange={handleFilterChange}
          onReset={handleResetFilters}
        />

        <TicketTable
          tickets={tickets}
          onSelectTicket={handleSelectTicket}
        />
      </main>

      {/* Ticket Modal */}
      {ticketDetail && (
        <TicketModal
          ticket={ticketDetail}
          role={role}
          authFetch={authFetch}
          onClose={() => {
            setTicketDetail(null);
            setSelectedTicketId(null);
          }}
          onRefresh={refreshAll}
        />
      )}

      {/* Inbound Email Simulate Modal */}
      <SimulateModal
        isOpen={isSimulateOpen}
        onClose={() => setIsSimulateOpen(false)}
        onIngested={refreshAll}
      />
    </div>
  );
}
