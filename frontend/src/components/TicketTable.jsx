import React from 'react';
import { Bot, UserCheck, Inbox } from 'lucide-react';

export default function TicketTable({ tickets, onSelectTicket }) {
  const getUrgencyBadge = (urgency) => {
    switch (urgency) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'HIGH':
        return 'bg-orange-100 text-orange-800 border-orange-300';
      case 'MEDIUM':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-300';
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800';
      case 'IN_PROGRESS':
        return 'bg-amber-100 text-amber-800';
      case 'WAITING_ON_CUSTOMER':
        return 'bg-purple-100 text-purple-800';
      case 'RESOLVED':
        return 'bg-emerald-100 text-emerald-800';
      case 'CLOSED':
        return 'bg-slate-200 text-slate-700';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse text-xs">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
              <th className="py-3 px-4">Ticket</th>
              <th className="py-3 px-4">Customer</th>
              <th className="py-3 px-4">Subject</th>
              <th className="py-3 px-4">Category</th>
              <th className="py-3 px-4">Urgency</th>
              <th className="py-3 px-4">Handling</th>
              <th className="py-3 px-4">Status</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {tickets && tickets.length > 0 ? (
              tickets.map((t) => (
                <tr
                  key={t.id}
                  onClick={() => onSelectTicket(t.id)}
                  className="hover:bg-slate-50 transition cursor-pointer"
                >
                  <td className="py-3 px-4 font-mono font-bold text-slate-900">
                    {t.ticketNumber}
                  </td>
                  <td className="py-3 px-4">
                    <div className="font-medium text-slate-900">{t.customerName}</div>
                    <div className="text-[10px] text-slate-400">{t.customerEmail}</div>
                  </td>
                  <td className="py-3 px-4 font-medium text-slate-800 max-w-xs truncate">
                    {t.subject}
                  </td>
                  <td className="py-3 px-4 text-slate-600">
                    {t.category ? t.category.replace('_', ' ') : ''}
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold border ${getUrgencyBadge(
                        t.urgency
                      )}`}
                    >
                      {t.urgency}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    {t.handlingType === 'AI_HANDLED' ? (
                      <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-indigo-100 text-indigo-800 border border-indigo-200 inline-flex items-center gap-1">
                        <Bot className="w-3 h-3 text-indigo-600" /> AI
                      </span>
                    ) : (
                      <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-purple-100 text-purple-800 border border-purple-200 inline-flex items-center gap-1">
                        <UserCheck className="w-3 h-3 text-purple-600" /> Human
                      </span>
                    )}
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold ${getStatusBadge(
                        t.status
                      )}`}
                    >
                      {t.status}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-right">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        onSelectTicket(t.id);
                      }}
                      className="px-2.5 py-1 bg-slate-100 hover:bg-indigo-50 text-indigo-600 rounded text-xs font-semibold transition border border-slate-200"
                    >
                      Inspect
                    </button>
                  </td>
                </tr>
              ))
            ) : null}
          </tbody>
        </table>
      </div>

      {(!tickets || tickets.length === 0) && (
        <div className="text-center py-12 text-slate-400">
          <Inbox className="w-8 h-8 mx-auto mb-2 text-slate-300" />
          <p className="text-sm">No tickets found matching current filters.</p>
        </div>
      )}
    </div>
  );
}
