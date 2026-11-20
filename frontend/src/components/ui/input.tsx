import * as React from "react";
import { cn } from "@/lib/utils";

const fieldClasses =
  "flex h-9 w-full rounded-control border border-border-strong bg-surface px-3 text-sm text-ink placeholder:text-ink-faint focus-visible:border-brand disabled:opacity-50";

function Input({ className, ...props }: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input className={cn(fieldClasses, className)} {...props} />;
}

function Select({
  className,
  ...props
}: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <select
      className={cn(
        fieldClasses,
        "select-chevron appearance-none pr-8",
        className,
      )}
      {...props}
    />
  );
}

export { Input, Select, fieldClasses };
