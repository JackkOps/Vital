import * as React from "react";
import { useOutletContext } from "react-router-dom";

export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string;
  description: string;
  actions?: React.ReactNode;
}) {
  const { agora } = useOutletContext<{ agora: Date }>();
  return (
    <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 className="text-[1.65rem] font-bold leading-tight text-ink">{title}</h1>
        <p className="mt-1 text-sm text-ink-muted">{description}</p>
      </div>
      <div className="flex items-center gap-3">
        {actions}
        <p className="pt-1 text-xs tabular text-ink-muted">
          {agora.toLocaleDateString("pt-BR", {
            weekday: "long",
            day: "numeric",
            month: "long",
          })}
        </p>
      </div>
    </header>
  );
}
