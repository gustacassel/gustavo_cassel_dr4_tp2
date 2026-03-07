# Build Pipeline Refactoring Kata

Este projeto é uma refatoração do código legado de um Pipeline de Build, baseado no Kata da Emily Bache. O objetivo foi transformar um código confuso e rígido em uma estrutura modular, testável e pronta para novas funcionalidades.

## 🧱 O Problema Original
O código inicial apresentava excesso de IFs aninhados, o que tornava a lógica difícil de seguir e perigosa de alterar. Além disso, as responsabilidades de teste, deploy e notificação estavam todas amontoadas em um único bloco de código.

## 🛠️ O que foi refatorado

### 1. Adeus aos IFs infinitos (Early Returns)
Substituí a estrutura de `if-else` gigante por **Cláusulas de Guarda (Early Returns)**. Agora, se uma etapa do pipeline falha (como os testes unitários), o método encerra imediatamente. Isso limpou o fluxo principal e eliminou o deslocamento do código para a direita.

### 2. Princípio "Tell, Don't Ask"
Refatorei a classe `Project` para que ela fosse mais inteligente. Em vez de o Pipeline ficar comparando Strings como `"success"`, eu movi essa lógica para dentro do modelo. Agora a Pipeline apenas "pergunta" se o deploy ou teste foi bem-sucedido via métodos booleanos, respeitando o encapsulamento.

### 3. Extração de Métodos e Coesão
O método `run` agora funciona apenas como um orquestrador. As lógicas específicas foram movidas para métodos privados bem definidos:
* `runUnitTests()`
* `executeDeploy()`
* `runSmokeTests()`
* `sendEmail()`



## 🚀 Nova Funcionalidade: Fluxo de Entrega Contínua
Além da limpeza, implementei o novo fluxo de deploy exigido:
1. **Unit Tests:** Bloqueia o pipeline se falhar.
2. **Staging Deploy:** Deploy em ambiente de teste.
3. **Smoke Tests:** Execução de testes de fumaça.
    * *Regra:* Se não houver smoke tests configurados, o pipeline falha propositalmente com a mensagem: `"Pipeline failed - no smoke tests"`.
4. **Production Deploy:** Realizado apenas se o Smoke Test passar.






## 🧪 Testes e Segurança
A refatoração foi guiada por testes unitários usando **JUnit 5** e **Mockito**.
* **Mocks:** Usei dublês de teste para as interfaces de E-mail, Logger e Config, permitindo validar as notificações sem disparar e-mails reais.
* **Cobertura:** Testei cenários de falha em cada etapa para garantir que o pipeline interrompe o fluxo corretamente sob qualquer erro.

## 💻 Como Rodar
O projeto usa Maven e Java 25.

```bash
# Rodar todos os testes
mvn test
