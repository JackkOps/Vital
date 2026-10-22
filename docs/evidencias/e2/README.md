# Evidências locais da E2

Logs capturados em 22/10/2026, preservados em UTF-8 fora de `target/`.
O histórico RED/GREEN não foi reescrito após a revisão independente.

| Arquivo | Revisão / execução | Resultado |
| --- | --- | --- |
| [red-us01.txt](red-us01.txt) | `6552fcb`, `mvnw.cmd test -q` | Saída 1; 61 testes, 4 falhas, nenhum erro |
| [green-us01.txt](green-us01.txt) | `dc2b990`, `mvnw.cmd test -q` | Saída 0; 61 testes, nenhuma falha/erro/skip |
| [revalidacao.txt](revalidacao.txt) | Correções posteriores à revisão de `4954c8d`, `mvnw.cmd clean test -q` | Saída 0; 72 testes, nenhuma falha/erro/skip |

O modo `-q` omite o resumo do Maven quando há sucesso. A contagem da revalidação
foi extraída dos 14 XMLs Surefire; o resumo por classe está em
[revalidacao-junit.xml](revalidacao-junit.xml). Dez dos 72 casos são cenários BDD.

Os testes de `SANGUE_TOTAL` acrescentados após a revisão são regressão posterior
ao GREEN. O BDD de cadastro é a evidência RED original desse novo enum.
Java local: 25.0.4, compilação configurada para Java 21. Banco de integração: H2.
Estes arquivos não comprovam execução no GitHub Actions nem proteção da branch.

## SHA-256 dos logs em UTF-8

```text
47C671746869FFCBA99E6467BA765F8C4C77BD2788AC9D7AAF00C43088EAAC18  green-us01.txt
C162A57E94AAF8A2304B5F2EAE7F5F7DB899019BBE66CEF84E67DBEE93C599BF  red-us01.txt
954F4D23FD54D65123BEBC27BFD717271BACACBDDB28D0B5317598D78E68469B  revalidacao.txt
```
