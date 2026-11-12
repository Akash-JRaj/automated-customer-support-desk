import React, { useState } from 'react';
import { X, Send, AlertTriangle, Key, Frown, Lightbulb } from 'lucide-react';

export default function SimulateModal({ isOpen, onClose, onIngested }) {
  const [senderName, setSenderName] = useState('Samantha Reed');
  const [senderEmail, setSenderEmail] = useState('samantha@techcorp.io');
  const [subject, setSubject] = useState('Payment webhook 500 error outage blocking transactions');
  const [body, setBody] = useState('Our production payment processing server is completely down with 500 internal server error. All customers are blocked immediately! Please fix this emergency.');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const loadPreset = (type) => {
    if (type === 'outage') {
      setSubject('CRITICAL: Database connection timeout in production cluster');
      setBody('We have a catastrophic production down emergency! Connection pool exhausted with 500 error outage across our API servers. Please resolve immediately!');
    } else if (type === 'password') {
      setSubject('Locked out of my account, need password reset link');
      setBody('Hi support, I forgot my account login credentials and my 2FA authentication is failing. Can you help me unlock my account?');
    } else if (type === 'frustrated') {
      setSubject('Unacceptable double charge on my subscription renewal!');
      setBody('You charged my credit card twice this month! This is terrible, unacceptable, and ridiculous. I demand a full refund and want to speak with a manager right now!');
    } else if (type === 'feature') {
      setSubject('Feature request: Dark mode theme support in dashboard');
      setBody('Hello team, would it be possible to add support for a dark mode color scheme in the customer portal? That would be a wonderful enhancement.');
    }
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/v1/emails/simulate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ senderName, senderEmail, subject, body })
      });
      const data = await res.json();
      alert(`Inbound email ingested!\nTicket: ${data.ticketNumber}\nClassification: ${data.category}\nUrgency: ${data.urgency}\nHandling: ${data.handlingType}`);
      onIngested();
      onClose();
    } catch (err) {
      console.error(err);
      alert('Failed to simulate email ingestion');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-xl w-full shadow-2xl border border-slate-200 overflow-hidden text-xs">
        <div className="px-6 py-4 bg-slate-50 border-b border-slate-200 flex items-center justify-between">
          <span className="text-base font-bold text-slate-900 flex items-center gap-2">
            <Send className="w-4 h-4 text-emerald-600" /> Simulate Inbound Customer Email
          </span>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div>
            <label className="block text-slate-500 font-medium mb-1">Quick Presets:</label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => loadPreset('outage')}
                className="p-2 border border-red-200 bg-red-50 hover:bg-red-100 rounded text-red-800 text-left transition"
              >
                <div className="font-bold flex items-center gap-1"><AlertTriangle className="w-3.5 h-3.5 text-red-600" /> Production Outage</div>
                <div className="text-[10px] text-red-600">Critical • Escalated</div>
              </button>

              <button
                type="button"
                onClick={() => loadPreset('password')}
                className="p-2 border border-blue-200 bg-blue-50 hover:bg-blue-100 rounded text-blue-800 text-left transition"
              >
                <div className="font-bold flex items-center gap-1"><Key className="w-3.5 h-3.5 text-blue-600" /> Password Reset</div>
                <div className="text-[10px] text-blue-600">Account • AI-Handled</div>
              </button>

              <button
                type="button"
                onClick={() => loadPreset('frustrated')}
                className="p-2 border border-purple-200 bg-purple-50 hover:bg-purple-100 rounded text-purple-800 text-left transition"
              >
                <div className="font-bold flex items-center gap-1"><Frown className="w-3.5 h-3.5 text-purple-600" /> Angry Double Charge</div>
                <div className="text-[10px] text-purple-600">Billing • Escalated</div>
              </button>

              <button
                type="button"
                onClick={() => loadPreset('feature')}
                className="p-2 border border-amber-200 bg-amber-50 hover:bg-amber-100 rounded text-amber-800 text-left transition"
              >
                <div className="font-bold flex items-center gap-1"><Lightbulb className="w-3.5 h-3.5 text-amber-600" /> Feature Request</div>
                <div className="text-[10px] text-amber-600">Low • AI-Handled</div>
              </button>
            </div>
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Customer Full Name</label>
            <input
              type="text"
              value={senderName}
              onChange={(e) => setSenderName(e.target.value)}
              className="w-full p-2 border border-slate-300 rounded-lg"
            />
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Customer Email</label>
            <input
              type="email"
              value={senderEmail}
              onChange={(e) => setSenderEmail(e.target.value)}
              className="w-full p-2 border border-slate-300 rounded-lg"
            />
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Subject</label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              className="w-full p-2 border border-slate-300 rounded-lg"
            />
          </div>

          <div>
            <label className="block text-slate-700 font-semibold mb-1">Email Body Content</label>
            <textarea
              rows={4}
              value={body}
              onChange={(e) => setBody(e.target.value)}
              className="w-full p-2 border border-slate-300 rounded-lg"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              onClick={onClose}
              className="px-4 py-2 border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50 transition"
            >
              Cancel
            </button>
            <button
              disabled={loading}
              onClick={handleSubmit}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg shadow transition flex items-center gap-1.5"
            >
              <Send className="w-3.5 h-3.5" /> Ingest & Classify Now
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
