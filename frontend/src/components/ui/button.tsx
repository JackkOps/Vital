import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

/**
 * Botões em pílula, como os chamados "Find Out More →" da referência.
 * A ação primária é sempre cobalto; o vermelho fica reservado a ações
 * destrutivas para não competir com sinais de emergência.
 */
const buttonVariants = cva(
  "group/btn inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-full font-medium transition-[background-color,color,border-color,transform] duration-150 active:translate-y-px disabled:pointer-events-none disabled:opacity-45 [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        primary: "bg-brand text-brand-foreground hover:bg-brand-hover",
        secondary:
          "border border-ink/80 bg-transparent text-ink hover:bg-ink hover:text-background",
        ghost: "text-ink-muted hover:bg-surface-muted hover:text-ink",
        danger: "bg-emergencia text-white hover:brightness-95",
        inverse: "bg-inverse-ink text-inverse hover:bg-white",
        link: "rounded-none px-0 text-brand underline-offset-4 hover:underline",
      },
      size: {
        sm: "h-8 px-3.5 text-[13px]",
        md: "h-10 px-5 text-sm",
        lg: "h-11 px-6 text-[0.95rem]",
        icon: "size-8 text-sm",
      },
    },
    defaultVariants: {
      variant: "primary",
      size: "md",
    },
  },
);

interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

function Button({ className, variant, size, asChild, ...props }: ButtonProps) {
  const Comp = asChild ? Slot : "button";
  return <Comp className={cn(buttonVariants({ variant, size }), className)} {...props} />;
}

/** Seta que desliza no hover do botão — o "→" das lâminas. */
function ButtonArrow({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 16"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={cn(
        "!h-3 !w-5 transition-transform duration-200 group-hover/btn:translate-x-0.5",
        className,
      )}
    >
      <path d="M1 8h21M15 1l7 7-7 7" />
    </svg>
  );
}

export { Button, ButtonArrow, buttonVariants };
