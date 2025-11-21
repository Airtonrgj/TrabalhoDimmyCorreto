package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {

    // DESAFIO 1
    @Override
    public Set<String> encontrarSessoesInvalidas(String s) throws IOException {
        Map<String, Stack<String>> sessoesDeUsuario = new HashMap<>();
        Set<String> sessoesInvalidas = new HashSet<>();

        try (BufferedReader arquivos = new BufferedReader(new FileReader(s))) {
            String linha = arquivos.readLine(); // ignora cabeçalho

            while ((linha = arquivos.readLine()) != null) {
                String[] campo = linha.split(",");

                // CORREÇÃO: Removido o ; e adicionado continue
                // Precisamos pelo menos até o índice 3 (ACTION_TYPE)
                if (campo.length < 4) continue;

                String USER_ID = campo[1];
                String SESSION_ID = campo[2];
                String ACTION_TYPE = campo[3];

                if (!sessoesDeUsuario.containsKey(USER_ID)) {
                    sessoesDeUsuario.put(USER_ID, new Stack<>());
                }
                Stack<String> sessoes = sessoesDeUsuario.get(USER_ID);

                if ("LOGIN".equals(ACTION_TYPE)) {
                    if (!sessoes.isEmpty()) {
                        sessoesInvalidas.add(SESSION_ID);
                    }
                    sessoes.push(SESSION_ID);
                } else if ("LOGOUT".equals(ACTION_TYPE)) {
                    if (sessoes.isEmpty() || !sessoes.peek().equals(SESSION_ID)) {
                        sessoesInvalidas.add(SESSION_ID);
                    } else {
                        sessoes.pop();
                    }
                }
            }
        }

        // VERIFICAÇÃO FINAL: Sessões que ficaram abertas na pilha também são inválidas
        for (Stack<String> pilha : sessoesDeUsuario.values()) {
            while(!pilha.isEmpty()) {
                sessoesInvalidas.add(pilha.pop());
            }
        }

        return sessoesInvalidas;
    }


    // DESAFIO 2
    @Override
    public List<String> reconstruirLinhaTempo(String arquivo, String sessionId) throws IOException {
        Queue<String> filaAcoes = new LinkedList<>();
        List<LogEvento> eventos = new ArrayList<>();

        try (BufferedReader arquivos = new BufferedReader(new FileReader(arquivo))) {
            String linha = arquivos.readLine();

            while ((linha = arquivos.readLine()) != null) {
                String[] campo = linha.split(",");

                // CORREÇÃO: ajustado tamanho mínimo
                if (campo.length < 4) continue;

                String TIMESTAMP = campo[0];
                String SESSION_ID = campo[2];
                String ACTION_TYPE = campo[3];

                if (SESSION_ID.equals(sessionId)) {
                    eventos.add(new LogEvento(TIMESTAMP, ACTION_TYPE));
                }
            }

            // Ordenação segura (comparando Longs é melhor que String para timestamp)
            eventos.sort(Comparator.comparingLong(e -> Long.parseLong(e.timestamp)));

            for (LogEvento e : eventos) {
                filaAcoes.add(e.actionType);
            }
        }
        return new ArrayList<>(filaAcoes);
    }

    class LogEvento {
        String timestamp;
        String actionType;
        public LogEvento(String timestamp, String actionType) {
            this.timestamp = timestamp;
            this.actionType = actionType;
        }
    }


    // DESAFIO 3
    @Override
    public List<Alerta> priorizarAlertas(String s, int n) throws IOException {
        List<Alerta> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(s))) {
            String linha = br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] campo = linha.split(",");

                // CORREÇÃO 1: Removido o ponto e vírgula e checa tamanho mínimo 6 (até severity)
                if (campo.length < 6) continue;

                long timestamp = Long.parseLong(campo[0]);
                String userId = campo[1];
                String sessionId = campo[2];
                String actionType = campo[3];
                String targetResource = campo[4];
                int severityLevel = Integer.parseInt(campo[5]);

                // CORREÇÃO 2: Tratamento seguro para o campo 6 (bytes) que pode não existir
                long bytesTransferred = 0;
                if (campo.length > 6 && !campo[6].isEmpty()) {
                    try {
                        bytesTransferred = Long.parseLong(campo[6]);
                    } catch (NumberFormatException e) {
                        bytesTransferred = 0;
                    }
                }

                lista.add(new Alerta(
                        timestamp,
                        userId,
                        sessionId,
                        actionType,
                        targetResource,
                        severityLevel,
                        bytesTransferred
                ));
            }
        }

        lista.sort((a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel()));

        if (n >= lista.size()) {
            return lista;
        }
        return lista.subList(0, n);
    }


    // DESAFIO 4
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String s) throws IOException {
        List<long[]> eventos = new ArrayList<>(10000);

        try (BufferedReader br = new BufferedReader(new FileReader(s))) {
            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }

                int firstComma = linha.indexOf(',');
                int sixthComma = -1;
                int count = 0;

                for (int i = 0; i < linha.length(); i++) {
                    if (linha.charAt(i) == ',') {
                        count++;
                        if (count == 6) {
                            sixthComma = i;
                            break;
                        }
                    }
                }

                if (firstComma != -1 && sixthComma != -1) {
                    try {
                        long timestamp = Long.parseLong(linha.substring(0, firstComma).trim());
                        int nextComma = linha.indexOf(',', sixthComma + 1);
                        int endIndex = (nextComma == -1) ? linha.length() : nextComma;

                        String bytesStr = linha.substring(sixthComma + 1, endIndex).trim();
                        if (!bytesStr.isEmpty()) {
                            long bytes = Long.parseLong(bytesStr);
                            if (bytes > 0) {
                                eventos.add(new long[]{timestamp, bytes});
                            }
                        }
                    } catch (Exception e) {
                        // Ignorar
                    }
                }
            }
        }

        Stack<Integer> stack = new Stack<>();
        Map<Long, Long> resultado = new HashMap<>();

        for (int i = eventos.size() - 1; i >= 0; i--) {
            long[] eventoAtual = eventos.get(i);
            long bytesAtual = eventoAtual[1];

            while (!stack.isEmpty()) {
                long[] eventoTopo = eventos.get(stack.peek());
                if (eventoTopo[1] <= bytesAtual) {
                    stack.pop();
                } else {
                    break;
                }
            }

            if (!stack.isEmpty()) {
                long[] proximoMaior = eventos.get(stack.peek());
                resultado.put(eventoAtual[0], proximoMaior[0]);
            }

            stack.push(i);
        }

        return resultado;
    }


    // DESAFIO 5
    @Override
    public Optional<List<String>> rastrearContaminacao(String arquivo, String org, String dest) throws IOException {
        Map<String, List<String>> recursosPorSessao = new HashMap<>();

        try (BufferedReader arquivos = new BufferedReader(new FileReader(arquivo))) {
            String linha = arquivos.readLine();

            while ((linha = arquivos.readLine()) != null) {
                String[] campo = linha.split(",");

                if (campo.length < 5) continue;

                String sessionId = campo[2];
                String recursoAcessado = campo[4];

                if (!recursosPorSessao.containsKey(sessionId)) {
                    recursosPorSessao.put(sessionId, new ArrayList<>());
                }
                recursosPorSessao.get(sessionId).add(recursoAcessado);
            }
        }

        Map<String, Set<String>> grafo = new HashMap<>(); // Mudei para Set para evitar duplicatas na lista de adj

        for (String sessao : recursosPorSessao.keySet()) {
            List<String> recursos = recursosPorSessao.get(sessao);
            for (int i = 0; i < recursos.size() - 1; i++) {
                String recursoAtual = recursos.get(i);
                String proximoRecurso = recursos.get(i + 1);

                if (!recursoAtual.equals(proximoRecurso)) { // Evita laço
                    grafo.computeIfAbsent(recursoAtual, k -> new HashSet<>()).add(proximoRecurso);
                }
            }
        }

        if (!grafo.containsKey(org)) return Optional.empty();
        if (org.equals(dest)) return Optional.of(Collections.singletonList(org));

        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Map<String, String> antecessor = new HashMap<>();

        fila.add(org);
        visitados.add(org);

        boolean achou = false;

        while (!fila.isEmpty()) {
            String recursoAtual = fila.poll();

            if (recursoAtual.equals(dest)) {
                achou = true;
                break;
            }

            if (grafo.containsKey(recursoAtual)) {
                for (String vizinho : grafo.get(recursoAtual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                        antecessor.put(vizinho, recursoAtual);
                    }
                }
            }
        }

        if (!achou) return Optional.empty();

        List<String> caminho = new ArrayList<>();
        String atual = dest;
        while (atual != null) {
            caminho.add(atual);
            atual = antecessor.get(atual);
        }
        Collections.reverse(caminho);

        return Optional.of(caminho);
    }
}