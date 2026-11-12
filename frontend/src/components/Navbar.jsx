import React from 'react';
import { Headset, BookOpen, Send, UserCheck } from 'lucide-react';

export default function Navbar({ onOpenSimulate, role, onRoleChange }) {
  return (
    <nav className="bg-indigo-900 text-white shadow-md sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-indigo-400 flex items-center justify-center text-white shadow-md">
              <Headset className="w-5 h-5" />
            </div>
            <div>
              <div className="text-lg font-bold tracking-tight flex items-center gap-2">
                SupportDesk <span className="text-indigo-300 font-semibold">AI</span>
                <span className="text-[10px] bg-indigo-800 text-indigo-200 px-2 py-0.5 rounded-full border border-indigo-700">
                  React 18 + Spring Boot
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center space-x-3 text-xs">
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="bg-indigo-800/80 hover:bg-indigo-700 text-indigo-100 px-3 py-1.5 rounded-lg border border-indigo-700 transition flex items-center gap-1.5"
            >
              <BookOpen className="w-3.5 h-3.5" /> API Docs
            </a>

            <button
              onClick={onOpenSimulate}
              className="bg-emerald-600 hover:bg-emerald-500 text-white font-medium px-3.5 py-1.5 rounded-lg shadow transition flex items-center gap-1.5"
            >
              <Send className="w-3.5 h-3.5" /> Simulate Inbound Email
            </button>

            <div className="flex items-center bg-indigo-950 px-2.5 py-1 rounded-lg border border-indigo-800">
              <UserCheck className="w-3.5 h-3.5 text-indigo-400 mr-1.5" />
              <select
                value={role}
                onChange={(e) => onRoleChange(e.target.value)}
                className="bg-transparent text-white font-medium focus:outline-none cursor-pointer"
              >
                <option value="ROLE_ADMIN" className="bg-slate-900">Admin (Akash)</option>
                <option value="ROLE_AGENT" className="bg-slate-900">Support Agent (Sarah)</option>
                <option value="ROLE_CUSTOMER" className="bg-slate-900">Customer (Read-Only)</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}
