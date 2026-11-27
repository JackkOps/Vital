import { cn } from "@/lib/utils";

interface Opcao<T extends string> {
  value: T;
  label: string;
}

/**
 * Grupo de opções únicas em pílulas — para campos curtos e fechados.
 * Navegável por setas, como um radiogroup nativo.
 */
export function ChoiceGroup<T extends string>({
  name,
  value,
  onChange,
  opcoes,
  className,
  mono,
}: {
  name: string;
  value: T | "";
  onChange: (value: T) => void;
  opcoes: Opcao<T>[];
  className?: string;
  mono?: boolean;
}) {
  function onKeyDown(e: React.KeyboardEvent<HTMLDivElement>) {
    if (!["ArrowRight", "ArrowLeft", "ArrowDown", "ArrowUp"].includes(e.key)) return;
    e.preventDefault();
    const atual = Math.max(0, opcoes.findIndex((o) => o.value === value));
    const passo = e.key === "ArrowRight" || e.key === "ArrowDown" ? 1 : -1;
    const proxima = opcoes[(atual + passo + opcoes.length) % opcoes.length];
    onChange(proxima.value);
    const botoes = e.currentTarget.querySelectorAll<HTMLButtonElement>("[role=radio]");
    botoes[(atual + passo + opcoes.length) % opcoes.length]?.focus();
  }

  return (
    <div
      role="radiogroup"
      aria-label={name}
      onKeyDown={onKeyDown}
      className={cn("flex flex-wrap gap-1.5", className)}
    >
      {opcoes.map((opcao, i) => {
        const ativo = value === opcao.value;
        return (
          <button
            key={opcao.value}
            type="button"
            role="radio"
            aria-checked={ativo}
            tabIndex={ativo || (!value && i === 0) ? 0 : -1}
            onClick={() => onChange(opcao.value)}
            className={cn(
              "h-8 rounded-full border px-3 text-[13px] font-medium transition-colors",
              mono && "min-w-11 font-mono text-[12.5px]",
              ativo
                ? "border-brand bg-brand text-brand-foreground"
                : "border-border-strong text-ink-muted hover:border-ink hover:text-ink",
            )}
          >
            {opcao.label}
          </button>
        );
      })}
    </div>
  );
}
