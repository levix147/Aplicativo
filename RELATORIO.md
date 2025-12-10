# Relatório de Arquitetura e Funcionalidades do Projeto GoPlan

Este documento detalha a função de cada classe, activity e elemento chave dentro do aplicativo "GoPlan".

---

## 1. Atividades (Telas do Aplicativo)

As `Activities` são as telas com as quais o usuário interage diretamente.

### `TelaPrincipal.java` (`activity_tela_principal.xml`)
- **Função:** É a tela de **Login** e o ponto de entrada do aplicativo.
- **Como Funciona:**
    - Ao iniciar, verifica com o Firebase Auth se o usuário já está logado. Se sim, redireciona para a `TeladeVisualizacao`.
    - Apresenta a opção de "Login com Google" que, ao ser clicada, inicia o fluxo de autenticação.
    - Após o sucesso, autentica o usuário no Firebase e o leva para a `TeladeVisualizacao`.
    - Contém campos de e-mail/senha (lógica de login com Firebase não implementada) e um link para a tela de cadastro.

### `FormCadastro.java` (`activity_form_cadastro.xml`)
- **Função:** Tela de **Cadastro** de novos usuários.
- **Como Funciona:**
    - Oferece a opção de "Cadastrar com Google", que cria uma nova conta no Firebase (ou faz login, se já existir).
    - Após o sucesso, leva o usuário diretamente para a `TeladeVisualizacao`.
    - O formulário de cadastro tradicional (e-mail/senha) não está funcional.

### `TeladeVisualizacao.java` (`activity_telade_visualizacao.xml`)
- **Função:** A **Tela Principal pós-login**, que exibe a lista de eventos.
- **Como Funciona:**
    - Usa um `RecyclerView` para mostrar uma lista vertical de eventos.
    - Conecta-se ao **Cloud Firestore** com um `addSnapshotListener`, tornando a lista **reativa em tempo real**.
    - Possui um campo de busca para filtrar os eventos pelo título.
    - Contém um botão flutuante ("+") que inicia a `AdicionarAtividade`.
    - Cada item na lista é clicável e leva para a `DetalheEventoActivity`.

### `AdicionarAtividade.java` (`activity_adicionar_atividade.xml`)
- **Função:** O formulário para **Criar um Novo Evento**.
- **Como Funciona:**
    - Coleta título e descrição.
    - Usa `DatePickerDialog` e `TimePickerDialog` para seleção de data e hora.
    - Ao clicar em "Local", inicia a `MapboxPickerActivity` para seleção no mapa.
    - Apresenta um `Switch` para "Adicionar ao Google Agenda?". Se ativado, usa a **API do Google Calendar** para criar o evento.
    - Ao publicar, salva o novo objeto `Tarefa` no **Cloud Firestore**.

### `MapboxPickerActivity.java` (`activity_mapbox_picker.xml`)
- **Função:** Tela dedicada para **Selecionar uma Localização no Mapa**.
- **Como Funciona:**
    - Exibe um mapa interativo do **Mapbox**.
    - Tenta centralizar na localização atual do usuário.
    - Um pino fixo no centro da tela marca o local. O usuário move o mapa sob o pino.
    - Ao confirmar, pega as coordenadas, usa o `Geocoder` para converter em endereço e retorna este endereço para a `AdicionarAtividade`.

### `DetalheEventoActivity.java` (`activity_detalhe_evento.xml`)
- **Função:** Exibe as **Informações Detalhadas** de um único evento.
- **Como Funciona:**
    - É iniciada ao clicar em um item na `TeladeVisualizacao`.
    - Recebe um objeto `Tarefa` contendo todos os dados.
    - Preenche os `TextViews` com as informações do evento.
    - No `MapView` do **Mapbox**, converte o endereço do evento em coordenadas e exibe um pino estático no local.

---

## 2. Classes de Dados e Lógica

Estas são as classes de suporte que não possuem interface visual.

### `Tarefa.java`
- **Função:** É o **Modelo de Dados** (Model) que representa um evento (título, descrição, data, etc.).
- **Como Funciona:**
    - É uma classe "POJO" que o Firestore usa para mapear dados.
    - Implementa `Parcelable`, tornando-se "transportável" entre Activities.

### `TarefaRepositorio.java`
- **Função:** A **Camada de Acesso a Dados** (Repository Pattern).
- **Como Funciona:**
    - Abstrai e centraliza a lógica de comunicação com o Firestore.
    - Contém o método `salvarTarefa()` para adicionar novos documentos.
    - Fornece a `CollectionReference` para que outras classes possam "ouvir" o banco de dados.

### `EventoAdapter.java`
- **Função:** O **Adaptador** para o `RecyclerView` da `TeladeVisualizacao`.
- **Como Funciona:**
    - Atua como uma ponte entre a `List<Tarefa>` e a lista visual na tela.
    - Cria a view de cada item (`onCreateViewHolder`) e a preenche com os dados da `Tarefa` (`onBindViewHolder`).
    - Configura o `setOnClickListener` em cada item para abrir a tela de detalhes.

---

## 3. Integrações e APIs

- **Firebase:** O backend do aplicativo, fornecendo:
    - **Authentication:** Para login com Google.
    - **Cloud Firestore:** Como banco de dados NoSQL em tempo real.
    - **Analytics:** Para coleta de dados de uso (automática).
- **Mapbox API:** Fornece toda a funcionalidade de mapas (exibição, interação).
- **Google Calendar API:** Permite a criação de eventos na agenda do usuário.
- **Google Play Services:** Usado para a autenticação com Google (`play-services-auth`) e para obter a localização inicial do usuário (`play-services-location`).
