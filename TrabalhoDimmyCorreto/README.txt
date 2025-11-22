br.edu.icev.aed.forense.MinhaAnaliseForense

# Trabalho 2 - AED

## Descrição
Este projeto consiste no desenvolvimento de uma ferramenta de **Análise Forense Digital** para processar logs de
sistemas comprometidos. O objetivo é identificar padrões anômalos, sessões inválidas e rastrear a origem de ataques
cibernéticos utilizando Estruturas de Dados avançadas.

O sistema processa arquivos de log (CSV) massivos para reconstruir a cadeia de eventos de um ataque.

## Objetivos e Desafios Solucionados
O trabalho foi dividido em 5 desafios principais, todos implementados na classe `MinhaAnaliseForense`:

1.  **Sessões Inválidas:** Detecção de incoerências em login/logout (ex: logout sem login prévio).
    * *Solução:* Uso de `Map` e `Stack` (Pilha) para controlar o estado das sessões.
2.  **Reconstrução de Linha do Tempo:** Ordenação cronológica das ações de um usuário específico.
    * *Solução:* Uso de `Queue` (Fila) e ordenação baseada em Timestamp.
3.  **Priorização de Alertas:** Listagem dos alertas mais críticos baseados em severidade.
    * *Solução:* Algoritmo de ordenação (`Comparator`) para classificar riscos.
4.  **Picos de Transferência:** Identificação do próximo pico de transferência de dados maior que o atual.
    * *Solução:* Implementação de **Monotonic Stack** para complexidade eficiente.
5.  **Rastrear Contaminação:** Mapeamento do caminho percorrido por uma ameaça entre recursos.
    * *Solução:* Modelagem em **Grafo** e busca em largura (**BFS**) para encontrar o caminho entre origem e destino.

## Tecnologias Utilizadas
* **Linguagem:** Java
* **IDE:** IntelliJ IDEA
* **Conceitos:** Estruturas de Dados (Pilhas, Filas, Mapas, Grafos, Árvores) e Manipulação de Arquivos (I/O).

## Estrutura do Projeto
A classe principal é a `MinhaAnaliseForense`, que implementa a interface `AnaliseForenseAvancada`.

```java
package br.edu.icev.aed.forense;
public class MinhaAnaliseForense implements AnaliseForenseAvancada { ... }

## Como Executar
Certifique-se de ter o arquivo forensic_logs.csv (ou outro arquivo de teste) disponível.

1. Importe o projeto no IntelliJ IDEA.

2. Execute a classe de testes (fornecida pelo professor) ou crie uma Main chamando os métodos da MinhaAnaliseForense.

3. Para gerar o artefato de entrega (.jar), utilize as configurações de build do IntelliJ incluindo as dependências.