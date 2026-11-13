import React, { useState } from 'react';
import { X, Bot, User, Cpu, Sparkles, Send, ArrowUpRight, ShieldAlert } from 'lucide-react';

export default function TicketModal({ ticket, role, authFetch, onClose, onRefresh }) {
  const [replyText, setReplyText] = useState('');
  const [resolveTicket, setResolveTicket] = useState(true);
  const [loading, setLoading] = useState(false);

  if (!ticket) return null;

  const isCustomerRole = role === 'ROLE_CUSTOMER';

  const handleApproveDraft = async () => {
    if (isCustomerRole) {
      alert('Action Restricted: You are currently viewing as "Customer (Read-Only)". Please switch role to "Admin" or "Support Agent" in the top navbar to approve AI drafts.');
      return;
    }

    setLoading(true);
    try {
      const res = await authFetch(`/api/v1/tickets/${ticket.id}/approve-draft`, { method: 'POST' });
      if (res.ok) {
        alert('AI Draft approved and dispatched to customer email!');
        onRefresh();
        onClose();
      } else {
        const errorText = await res.text();
        alert(`Failed to approve draft (${res.status}): ${errorText || 'Unauthorized or Forbidden'}`);
      }
    } catch (err) {
      console.error(err);
      alert('Network error while approving draft');
    } finally {
      setLoading(false);
    }
  };

  const handleSendReply = async () => {
    if (isCustomerRole) {
      alert('Action Restricted: Switch role to "Admin" or "Support Agent" in the navbar to send replies.');
      return;
    }
    if (!replyText.trim()) return alert('Please enter reply text');

    setLoading(true);
    try {
      const res = await authFetch(`/api/v1/tickets/${ticket.id}/reply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: replyText, resolveTicket })
      });
      if (res.ok) {
        alert('Reply dispatched to customer!');
        onRefresh();
        onClose();
      } else {
        const errorText = await res.text();
        alert(`Failed to send reply (${res.status}): ${errorText || 'Unauthorized or Forbidden'}`);
      }
    } catch (err) {
      console.error(err);
      alert('Network error while sending reply');
    } finally {
      setLoading(false);
    }
  };

  const handleEscalate = async () => {
    if (isCustomerRole) {
      alert('Action Restricted: Switch role to "Admin" or "Support Agent" in the navbar to escalate.');
      return;
    }
    const reason = prompt('Enter escalation reason:', 'Manually escalated for tier-2 specialist review');
    if (!reason) return;

    setLoading(true);
    try {
      const res = await authFetch(`/api/v1/tickets/${ticket.id}/escalate?reason=${encodeURIComponent(reason)}`, {
        method: 'POST'
      });
      if (res.ok) {
        alert('Ticket escalated to Tier-2 specialist!');
        onRefresh();
        onClose();
      } else {
        alert('Failed to escalate ticket');
      }
    } catch (err) {
      console.error(err);
      alert('Network error while escalating');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[92vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden text-xs">
        {/* Header */}
        <div className="px-6 py-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-base font-bold text-slate-900 font-mono">
              {ticket.ticketNumber}
            </span>
            <span className="px-2 py-0.5 rounded text-xs font-semibold bg-slate-200 text-slate-800">
              {ticket.urgency}
            </span>
            <span className="px-2 py-0.5 rounded text-xs font-semibold bg-indigo-100 text-indigo-800 border border-indigo-200">
              {ticket.handlingType === 'AI_HANDLED' ? 'AI-Handled' : 'Human Escalated'}
            </span>
            {isCustomerRole && (
              <span className="px-2 py-0.5 rounded text-[10px] font-medium bg-amber-100 text-amber-800 border border-amber-200 flex items-center gap-1">
                <ShieldAlert className="w-3 h-3 text-amber-600" /> Read-Only Role
              </span>
            )}
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1">
          {/* Metadata */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
            <h3 className="text-base font-bold text-slate-900 mb-1">{ticket.subject}</h3>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-500">
              <span>Customer: <strong className="text-slate-700">{ticket.customerName}</strong></span>
              <span>Email: <strong className="text-slate-700">{ticket.customerEmail}</strong></span>
              <span>Created: {new Date(ticket.createdAt).toLocaleString()}</span>
            </div>

            {/* AI Banner */}
            <div className="mt-3 pt-3 border-t border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center gap-2 text-indigo-700">
                <Cpu className="w-4 h-4" />
                <span>AI Confidence: <strong>{Math.round((ticket.aiConfidenceScore || 0.85) * 100)}%</strong></span>
              </div>
              {ticket.escalationReason && (
                <div className="text-purple-700 italic">
                  Escalation Trigger: {ticket.escalationReason}
                </div>
              )}
            </div>
          </div>

          {/* Conversation history */}
          <div>
            <h4 className="font-bold text-slate-700 uppercase tracking-wider mb-3">Conversation Thread</h4>
            <div className="space-y-3">
              {ticket.messages && ticket.messages.map((m) => {
                const isCustomer = m.senderType === 'CUSTOMER';
                const isAi = m.senderType === 'AI_SYSTEM';

                return (
                  <div
                    key={m.id}
                    className={`p-3 rounded-xl border ${
                      isCustomer
                        ? 'bg-slate-50 border-slate-200'
                        : isAi
                        ? 'bg-indigo-50/70 border-indigo-200 ml-4'
                        : 'bg-emerald-50/70 border-emerald-200 ml-4'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1.5 font-semibold text-slate-700">
                      <span className="flex items-center gap-1.5">
                        {isCustomer ? <User className="w-3.5 h-3.5 text-slate-500" /> : isAi ? <Bot className="w-3.5 h-3.5 text-indigo-600" /> : <User className="w-3.5 h-3.5 text-emerald-600" />}
                        {m.senderName} <span className="text-[10px] text-slate-400 font-normal">({m.senderType})</span>
                      </span>
                      <span className="text-[10px] text-slate-400">{new Date(m.sentAt).toLocaleTimeString()}</span>
                    </div>
                    <div className="whitespace-pre-wrap text-slate-800 leading-relaxed">{m.body}</div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* AI Pre-Drafted Reply */}
          {ticket.draftReply && (
            <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4 space-y-2">
              <div className="flex items-center justify-between">
                <span className="font-bold text-indigo-900 flex items-center gap-1.5">
                  <Sparkles className="w-4 h-4 text-indigo-600" /> AI-Drafted Solution
                </span>
                <button
                  disabled={loading}
                  onClick={handleApproveDraft}
                  className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium px-3 py-1 rounded text-xs shadow-sm transition flex items-center gap-1"
                >
                  <Send className="w-3 h-3" /> Approve & Send Draft
                </button>
              </div>
              <pre className="text-xs text-indigo-950 font-sans whitespace-pre-wrap bg-white p-3 rounded-lg border border-indigo-100">
                {ticket.draftReply}
              </pre>
            </div>
          )}

          {/* Agent Reply Box */}
          <div className="space-y-2">
            <label className="font-bold text-slate-700 flex items-center gap-1.5">
              <Send className="w-3.5 h-3.5" /> Send Agent Reply to Customer
            </label>
            <textarea
              rows={4}
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Type your response here..."
              className="w-full text-xs p-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <div className="flex items-center justify-between pt-1">
              <label className="flex items-center gap-2 cursor-pointer text-slate-600">
                <input
                  type="checkbox"
                  checked={resolveTicket}
                  onChange={(e) => setResolveTicket(e.target.checked)}
                  className="rounded text-indigo-600"
                />
                <span>Mark ticket as RESOLVED upon dispatch</span>
              </label>

              <div className="flex items-center gap-2">
                <button
                  onClick={handleEscalate}
                  className="px-3 py-1.5 border border-purple-300 text-purple-700 hover:bg-purple-50 rounded-lg text-xs font-medium transition flex items-center gap-1"
                >
                  <ArrowUpRight className="w-3.5 h-3.5" /> Escalate Ticket
                </button>
                <button
                  disabled={loading}
                  onClick={handleSendReply}
                  className="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-medium shadow transition flex items-center gap-1.5"
                >
                  <Send className="w-3.5 h-3.5" /> Send Reply
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
