import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

/**
 * Selo de status em pílula. Nunca usar só a cor: o conteúdo (ícone + texto)
 * é que carrega o significado — a cor apenas reforça.
 */
const badgeVariants = cva(
  "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11.5px] font-medium leading-5 tabular",
  {
    variants: {
      tone: {
        neutral: "bg-surface-muted text-ink-muted",
        brand: "bg-brand text-brand-foreground",
        ok: "bg-ok-soft text-ok",
        alerta: "bg-alerta-soft text-alerta",
        emergencia: "bg-emergencia-soft text-emergencia",
        info: "bg-info-soft text-info",
        hem: "bg-hem-soft text-hem",
        pla: "bg-pla-soft text-pla",
        plq: "bg-plq-soft text-plq",
        st: "bg-st-soft text-st",
        ink: "bg-ink text-background",
      },
    },
    defaultVariants: { tone: "neutral" },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, tone, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ tone }), className)} {...props} />;
}

export { Badge, badgeVariants };
