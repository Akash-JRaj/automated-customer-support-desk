import React from 'react';
import { Inbox, TrendingUp, Bot, UserCog, Timer } from 'lucide-react';

export default function KpiCards({ metrics }) {
  const m = metrics || {
    totalTickets: 0,
    resolutionRate: 0,
    aiAutonomyRate: 0,
    aiHandledCount: 0,
    humanEscalatedCount: 0,
    averageResolutionMinutes: 0
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      {/* Total Tickets */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 flex flex-col justify-between">
        <div className="flex items-center justify-between text-slate-500 text-xs font-semibold uppercase tracking-wider">
          <span>Total Ingested</span>
          <Inbox className="w-4 h-4 text-slate-400" />
        </div>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-3xl font-extrabold text-slate-900">{m.totalTickets}</span>
          <span className="text-xs text-slate-500">tickets</span>
        </div>
        <div className="mt-2 text-xs text-slate-400">Total customer cases parsed</div>
      </div>

      {/* Resolution Rate */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 flex flex-col justify-between">
        <div className="flex items-center justify-between text-slate-500 text-xs font-semibold uppercase tracking-wider">
          <span>Resolution Rate</span>
          <TrendingUp className="w-4 h-4 text-emerald-500" />
        </div>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-3xl font-extrabold text-emerald-600">{m.resolutionRate}%</span>
        </div>
        <div className="w-full bg-slate-100 rounded-full h-1.5 mt-2 overflow-hidden">
          <div
            className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500"
            style={{ width: `${Math.min(100, m.resolutionRate || 0)}%` }}
          />
        </div>
      </div>

      {/* AI Autonomy */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 flex flex-col justify-between border-l-4 border-l-indigo-600">
        <div className="flex items-center justify-between text-indigo-700 text-xs font-semibold uppercase tracking-wider">
          <span>AI Autonomy</span>
          <Bot className="w-4 h-4 text-indigo-600" />
        </div>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-3xl font-extrabold text-indigo-600">{m.aiAutonomyRate}%</span>
          <span className="text-xs text-slate-500">({m.aiHandledCount} cases)</span>
        </div>
        <div className="mt-2 text-xs text-indigo-700">Autonomous reply & resolution</div>
      </div>

      {/* Human Escalations */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 flex flex-col justify-between border-l-4 border-l-purple-600">
        <div className="flex items-center justify-between text-purple-700 text-xs font-semibold uppercase tracking-wider">
          <span>Human Escalations</span>
          <UserCog className="w-4 h-4 text-purple-600" />
        </div>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-3xl font-extrabold text-purple-600">{m.humanEscalatedCount}</span>
          <span className="text-xs text-slate-500">cases</span>
        </div>
        <div className="mt-2 text-xs text-purple-700">Critical incidents & complex tier-2</div>
      </div>

      {/* Avg Resolution Time */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-slate-200 flex flex-col justify-between">
        <div className="flex items-center justify-between text-slate-500 text-xs font-semibold uppercase tracking-wider">
          <span>Avg Response Time</span>
          <Timer className="w-4 h-4 text-amber-500" />
        </div>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-3xl font-extrabold text-amber-600">{m.averageResolutionMinutes}</span>
          <span className="text-xs text-slate-500">mins</span>
        </div>
        <div className="mt-2 text-xs text-slate-400">Mean time to initial response</div>
      </div>
    </div>
  );
}
