import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class MinhaAnaliseForense implements AnaliseForenseAvancada{

    @Override
    public Set<String> encontrarSessoesInvalidas(String s) throws IOException {
        // Implementar usando Map<String, Stack<String>>
        // A tarefa do desafio é Encontrar Sessões Inválidas, portando a implementação pensada foi a de
        // Uma pilha de login e uma de logaut usando map que server como um gerneciador de diretórios, ou seja o loguin do joão não interfere em nada o da Maria por exemplo

        //Cria uma pilha para cada usuário basicamente
        Map<String, Stack<String>> sessoesDeUsuario = new HashMap<>();

        //vai coletar todos os IDs de sessões invalidas, sem nenhuma ordem especifica, por que isso vai ser implemntado no desafio 2. coletas as sessões invalidas sem duplicataaas
        Set<String> sessoesInvalidas = new HashSet<>();

        //como ele vai ler o arquivo CSV
        try (BufferedReader arquivos = new BufferedReader(new FileReader("input/arquivo_logs.csv"))) {
            String linha = arquivos.readLine();   //ignora o cabeçalho

            //busca da informações linha por linha até chegar na última Linha
            while ((linha = arquivos.readLine()) != null) {
                String[] campo = linha.split(",");  //divide a string por virgula

                String USER_ID = campo[1];
                String SESSION_ID = campo[2];
                String ACTION_TYPE = campo[3];

                //se o usuario não tiver uma pilha ainda então ela é criada
                if (!sessoesDeUsuario.containsKey(USER_ID)) {
                    sessoesDeUsuario.put(USER_ID, new Stack<>());
                }
                //aqui ele cria a pilha se necessário
                Stack<String> sessoes = sessoesDeUsuario.get(USER_ID);

                //Aqui é o caso do tipo de ação ser igual ou não a de LOGIN. Poderia ser feita baseada no logout tbm
                if ("LOGIN".equals(ACTION_TYPE)) {
                    //SE JA TIVER UM LOGIN então vai para sessões invalidas, ja que não pode ter um login seguido de outro
                    if (!sessoes.isEmpty()) {
                        sessoesInvalidas.add(SESSION_ID);
                    }
                    //se não tiver então ele empilha normalmente nas sessoes
                    sessoes.push(SESSION_ID);
                } else if ("LOGOUT".equals(ACTION_TYPE)) {
                    if (sessoes.isEmpty() || !sessoes.peek().equals(SESSION_ID)) {
                        sessoesInvalidas.add(SESSION_ID);
                    } else {
                        //esse logout não tem problemas então é so desempilhar
                        sessoes.pop();
                    }


                }
            }
        }
        return sessoesInvalidas;
    }

    @Override
    public List<String> reconstruirLinhaTempo(String arquivo, String sessionId) throws IOException {

        Queue<String> filaAcoes = new LinkedList<>();
        List<LogEvento> eventos = new ArrayList<>();

        try (BufferedReader arquivos = new BufferedReader(new FileReader(arquivo))) {

            String linha = arquivos.readLine(); // ignora cabeçalho

            while ((linha = arquivos.readLine()) != null) {

                String[] campo = linha.split(",");

                String TIMESTAMP = campo[0];
                String SESSION_ID = campo[2];
                String ACTION_TYPE = campo[3];

                if (SESSION_ID.equals(sessionId)) {
                    eventos.add(new LogEvento(TIMESTAMP, ACTION_TYPE));
                }
            }

            eventos.sort(Comparator.comparing(e -> e.timestamp));

            for (LogEvento e : eventos) {
                filaAcoes.add(e.actionType);
            }

        }

        return new ArrayList<>(filaAcoes);
    }


    // Classe auxiliar
    class LogEvento {
        String timestamp;
        String actionType;

        public LogEvento(String timestamp, String actionType) {
            this.timestamp = timestamp;
            this.actionType = actionType;
        }
    }

    @Override
    public List<Alerta> priorizarAlertas(String s, int i) throws IOException {
        return List.of();
    }

    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String s) throws IOException {
        // OTIMIZAÇÃO: Lista com long[] nativo e capacidade pré-alocada
        List<long[]> eventos = new ArrayList<>(10000);

        // Ler arquivo CSV
        try (BufferedReader br = new BufferedReader(new FileReader("arquivo"))) {
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
                        long bytes = Long.parseLong(linha.substring(sixthComma + 1, endIndex).trim());

                        if (bytes > 0) {
                            eventos.add(new long[]{timestamp, bytes});
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar linhas inválidas
                    }
                }
            }
        }

        // Lógica da Stack Monotônica com índices para máxima performance
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

    @Override
    public Optional<List<String>> rastrearContaminacao(String arquivo, String org, String dest) throws IOException {
        Map<String, List<String>> recursosPorSessao = new HashMap<>();

        try (BufferedReader arquivos = new BufferedReader(new FileReader("arquivo"))) {
            String linha = arquivos.readLine();

            while ((linha = arquivos.readLine()) != null) {
                String[] campo = linha.split(",");

                String sessionId = campo[2];
                String recursoAcessado = campo[4];

                if (!recursosPorSessao.containsKey(sessionId)) {
                    recursosPorSessao.put(sessionId, new ArrayList<>());
                }

                recursosPorSessao.get(sessionId).add(recursoAcessado);
            }
        }

        // monta o grafo com as conexoes entre recursos
        Map<String, List<String>> grafo = new HashMap<>();

        for (String sessao : recursosPorSessao.keySet()) {
            List<String> recursos = recursosPorSessao.get(sessao);

            // se um recurso A vem antes de B na mesma sessao, cria aresta A -> B
            for (int i = 0; i < recursos.size() - 1; i++) {
                String recursoAtual = recursos.get(i);
                String proximoRecurso = recursos.get(i + 1);

                if (!grafo.containsKey(recursoAtual)) {
                    grafo.put(recursoAtual, new ArrayList<>());
                }

                if (!grafo.get(recursoAtual).contains(proximoRecurso)) {
                    grafo.get(recursoAtual).add(proximoRecurso);
                }
            }
        }

        if (!grafo.containsKey(org)) {
            return Optional.empty();
        }

        // caso especial: org = dest
        if (org.equals(dest)) {
            List<String> caminhoUnico = new ArrayList<>();
            caminhoUnico.add(org);
            return Optional.of(caminhoUnico);
        }

        // BFS pra achar o caminho mais curto
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
                List<String> vizinhos = grafo.get(recursoAtual);

                for (String vizinho : vizinhos) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                        antecessor.put(vizinho, recursoAtual);
                    }
                }
            }
        }

        if (!achou) {
            return Optional.empty();
        }

        // reconstroi o caminho voltando do dest ate a org
        List<String> caminho = new ArrayList<>();
        String atual = dest;

        while (atual != null) {
            caminho.add(atual);
            atual = antecessor.get(atual);
        }

        // inverte pra ficar na ordem certa
        List<String> caminhoCorreto = new ArrayList<>();
        for (int i = caminho.size() - 1; i >= 0; i--) {
            caminhoCorreto.add(caminho.get(i));
        }

        return Optional.of(caminhoCorreto);
    }
}
