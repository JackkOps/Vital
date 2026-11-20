import { cn } from "@/lib/utils";

interface Opcao<T extends string> {
  value: T;
  label: string;
}

/** Grupo de opções únicas em formato de pílula — usado para campos curtos e fechados. */
export function ChoiceGroup<T extends string>({
  name,
  value,
  onChange,
  opcoes,
  className,
}: {
  name: string;
  value: T | "";
  onChange: (value: T) => void;
  opcoes: Opcao<T>[];
  className?: string;
}) {
  return (
    <div role="radiogroup" aria-label={name} className={cn("flex flex-wrap gap-1.5", className)}>
      {opcoes.map((opcao) => {
        const ativo = value === opcao.value;
        return (
          <button
            key={opcao.value}
            type="button"
            role="radio"
            aria-checked={ativo}
            onClick={() => onChange(opcao.value)}
            className={cn(
              "rounded-control border px-2.5 py-1 text-xs font-semibold transition-colors",
              ativo
                ? "border-brand bg-brand-soft text-brand"
                : "border-border-strong bg-surface text-ink-muted hover:bg-surface-muted",
            )}
          >
            {opcao.label}
          </button>
        );
      })}
    </div>
  );
}
