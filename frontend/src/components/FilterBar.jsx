import React, { useState, useEffect } from 'react';
import { Search, RotateCcw, Bot, ShieldAlert, X } from 'lucide-react';

export default function FilterBar({
  filters,
  onFilterChange,
  onReset
}) {
  const [localSearch, setLocalSearch] = useState(filters.search || '');

  // Keep localSearch in sync if filters.search is reset externally
  useEffect(() => {
    setLocalSearch(filters.search || '');
  }, [filters.search]);

  // Debounce search update to parent
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localSearch !== filters.search) {
        onFilterChange('search', localSearch);
      }
    }, 250);
    return () => clearTimeout(timer);
  }, [localSearch, filters.search, onFilterChange]);

  const handleClearSearch = () => {
    setLocalSearch('');
    onFilterChange('search', '');
  };

  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 space-y-4">
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
        {/* Handling Tabs */}
        <div className="flex bg-slate-100 p-1 rounded-lg w-full sm:w-auto">
          <button
            onClick={() => onFilterChange('handlingType', '')}
            className={`px-3.5 py-1.5 text-xs font-medium rounded-md transition ${
              filters.handlingType === ''
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            All Tickets
          </button>
          <button
            onClick={() => onFilterChange('handlingType', 'AI_HANDLED')}
            className={`px-3.5 py-1.5 text-xs font-medium rounded-md transition flex items-center gap-1.5 ${
              filters.handlingType === 'AI_HANDLED'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Bot className="w-3.5 h-3.5 text-indigo-500" /> AI-Handled
          </button>
          <button
            onClick={() => onFilterChange('handlingType', 'HUMAN_ESCALATED')}
            className={`px-3.5 py-1.5 text-xs font-medium rounded-md transition flex items-center gap-1.5 ${
              filters.handlingType === 'HUMAN_ESCALATED'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <ShieldAlert className="w-3.5 h-3.5 text-purple-500" /> Human Escalations
          </button>
        </div>

        {/* Search */}
        <div className="relative w-full sm:w-80">
          <input
            type="text"
            value={localSearch}
            onChange={(e) => setLocalSearch(e.target.value)}
            placeholder="Search by ticket #, customer, subject..."
            className="w-full pl-9 pr-8 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white"
          />
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-2.5" />
          {localSearch && (
            <button
              onClick={handleClearSearch}
              className="absolute right-2.5 top-2.5 text-slate-400 hover:text-slate-600"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>

      {/* Secondary Dropdown Filters */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 border-t border-slate-100 text-xs">
        <div>
          <label className="block text-slate-500 mb-1 font-medium">Status</label>
          <select
            value={filters.status}
            onChange={(e) => onFilterChange('status', e.target.value)}
            className="w-full bg-slate-50 border border-slate-200 rounded-md p-1.5 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="">All Statuses</option>
            <option value="NEW">NEW</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="WAITING_ON_CUSTOMER">WAITING_ON_CUSTOMER</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="CLOSED">CLOSED</option>
          </select>
        </div>

        <div>
          <label className="block text-slate-500 mb-1 font-medium">Urgency</label>
          <select
            value={filters.urgency}
            onChange={(e) => onFilterChange('urgency', e.target.value)}
            className="w-full bg-slate-50 border border-slate-200 rounded-md p-1.5 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="">All Urgencies</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="HIGH">HIGH</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="LOW">LOW</option>
          </select>
        </div>

        <div>
          <label className="block text-slate-500 mb-1 font-medium">Category</label>
          <select
            value={filters.category}
            onChange={(e) => onFilterChange('category', e.target.value)}
            className="w-full bg-slate-50 border border-slate-200 rounded-md p-1.5 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="">All Categories</option>
            <option value="BILLING">Billing & Payments</option>
            <option value="TECHNICAL_SUPPORT">Technical Support</option>
            <option value="ACCOUNT_ACCESS">Account Access</option>
            <option value="SECURITY">Security Issues</option>
            <option value="FEATURE_REQUEST">Feature Request</option>
            <option value="GENERAL_INQUIRY">General Inquiry</option>
          </select>
        </div>

        <div className="flex items-end">
          <button
            onClick={onReset}
            className="w-full py-1.5 px-3 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-md text-xs font-medium transition flex items-center justify-center gap-1.5"
          >
            <RotateCcw className="w-3.5 h-3.5" /> Reset Filters
          </button>
        </div>
      </div>
    </div>
  );
}
