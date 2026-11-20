export function formatarData(iso: string | null | undefined): string {
  if (!iso) return "—";
  const [ano, mes, dia] = iso.split("T")[0].split("-");
  return `${dia}/${mes}/${ano}`;
}

export function formatarDataHora(iso: string | null | undefined): string {
  if (!iso) return "—";
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return "—";
  return data.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatarPercentual(valor: number | null | undefined, digitos = 0): string {
  if (valor === null || valor === undefined) return "—";
  return `${(valor * 100).toLocaleString("pt-BR", { maximumFractionDigits: digitos })}%`;
}

export function formatarNumero(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return "—";
  return valor.toLocaleString("pt-BR");
}

export function formatarHoras(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return "—";
  return `${valor.toLocaleString("pt-BR", { maximumFractionDigits: 1 })} h`;
}
