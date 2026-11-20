import * as React from "react";
import { cn } from "@/lib/utils";
import { AlertTriangle, CheckCircle2, Info, X } from "lucide-react";

type ToastTone = "ok" | "erro" | "info";

interface ToastItem {
  id: number;
  title: string;
  description?: string;
  tone: ToastTone;
}

interface ToastContextValue {
  toast: (item: Omit<ToastItem, "id">) => void;
}

const ToastContext = React.createContext<ToastContextValue | null>(null);

const toneConfig: Record<ToastTone, { icon: React.ElementType; className: string }> = {
  ok: { icon: CheckCircle2, className: "border-ok/30 text-ok" },
  erro: { icon: AlertTriangle, className: "border-emergencia/30 text-emergencia" },
  info: { icon: Info, className: "border-info/30 text-info" },
};

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = React.useState<ToastItem[]>([]);

  const toast = React.useCallback((item: Omit<ToastItem, "id">) => {
    const id = Date.now() + Math.random();
    setItems((prev) => [...prev, { ...item, id }]);
    window.setTimeout(() => {
      setItems((prev) => prev.filter((t) => t.id !== id));
    }, 4500);
  }, []);

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}
      <div className="pointer-events-none fixed bottom-4 right-4 z-[60] flex w-full max-w-sm flex-col gap-2">
        {items.map((item) => {
          const tone = toneConfig[item.tone];
          const Icon = tone.icon;
          return (
            <div
              key={item.id}
              role="status"
              className={cn(
                "pointer-events-auto flex items-start gap-3 rounded-panel border bg-surface p-3.5 shadow-[0_12px_32px_rgba(27,36,48,0.14)]",
                tone.className,
              )}
            >
              <Icon className="mt-0.5 size-4 shrink-0" />
              <div className="flex-1 text-sm">
                <p className="font-semibold text-ink">{item.title}</p>
                {item.description ? (
                  <p className="mt-0.5 text-ink-muted">{item.description}</p>
                ) : null}
              </div>
              <button
                type="button"
                onClick={() => setItems((prev) => prev.filter((t) => t.id !== item.id))}
                className="text-ink-faint hover:text-ink"
              >
                <X className="size-3.5" />
                <span className="sr-only">Fechar</span>
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = React.useContext(ToastContext);
  if (!ctx) throw new Error("useToast precisa estar dentro de ToastProvider");
  return ctx;
}
