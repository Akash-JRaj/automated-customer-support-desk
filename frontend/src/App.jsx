import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import KpiCards from './components/KpiCards';
import FilterBar from './components/FilterBar';
import TicketTable from './components/TicketTable';
import TicketModal from './components/TicketModal';
import SimulateModal from './components/SimulateModal';

export default function App() {
  const [role, setRole] = useState('ROLE_ADMIN');
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

  const fetchMetrics = useCallback(async () => {
    try {
      const res = await fetch('/api/v1/dashboard/metrics');
      if (res.ok) {
        const data = await res.json();
        setMetrics(data);
      }
    } catch (err) {
      console.error('Failed to fetch metrics:', err);
    }
  }, []);

  const fetchTickets = useCallback(async () => {
    try {
      let url = `/api/v1/tickets?size=50`;
      if (filters.status) url += `&status=${filters.status}`;
      if (filters.urgency) url += `&urgency=${filters.urgency}`;
      if (filters.category) url += `&category=${filters.category}`;
      if (filters.handlingType) url += `&handlingType=${filters.handlingType}`;
      if (filters.search) url += `&search=${encodeURIComponent(filters.search)}`;

      const res = await fetch(url);
      if (res.ok) {
        const page = await res.json();
        setTickets(page.content || []);
      }
    } catch (err) {
      console.error('Failed to fetch tickets:', err);
    }
  }, [filters]);

  useEffect(() => {
    fetchMetrics();
    fetchTickets();
  }, [fetchMetrics, fetchTickets]);

  const handleSelectTicket = async (id) => {
    try {
      setSelectedTicketId(id);
      const res = await fetch(`/api/v1/tickets/${id}`);
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
        onRoleChange={(r) => {
          setRole(r);
          alert(`Switched active view role to: ${r}`);
        }}
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
