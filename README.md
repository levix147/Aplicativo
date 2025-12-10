# GoPlan - Aplicativo de Gerenciamento de Eventos para Android

GoPlan é um aplicativo Android desenvolvido em Java que funciona como um quadro Kanban para o gerenciamento de tarefas e eventos. O projeto demonstra a integração com várias APIs modernas e segue as melhores práticas de desenvolvimento Android.

## Funcionalidades Principais

- **Autenticação de Usuários**: Login rápido e seguro utilizando a conta do Google (Firebase Authentication).
- **Quadro Kanban Interativo**: Organize tarefas visualmente em colunas "A Fazer", "Fazendo" e "Concluído" com uma interface de arrastar e soltar (Drag and Drop).
- **Banco de Dados em Tempo Real**: As tarefas são sincronizadas instantaneamente entre dispositivos usando o Cloud Firestore do Firebase.
- **Criação de Eventos**: Formulário completo para adicionar novos eventos com título, descrição, data e hora.
- **Seleção de Local com Mapa**: Integração com a API do **Mapbox** para permitir que o usuário selecione a localização do evento em um mapa interativo.
- **Integração com Google Calendar**: Opção para adicionar o evento criado diretamente na agenda do Google do usuário.
- **Segurança de API Keys**: As chaves de API são protegidas e não são expostas no código-fonte, sendo carregadas a partir de um arquivo `local.properties` ignorado pelo Git.

## Como Executar o Projeto

1.  **Clone o Repositório**
    ```sh
    git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
    ```

2.  **Configure o Firebase**
    - Crie um novo projeto no [Firebase Console](https://console.firebase.google.com/).
    - Adicione um aplicativo Android ao seu projeto com o nome de pacote `com.example.aplicativo`.
    - Baixe o arquivo `google-services.json` e coloque-o na pasta `app/` do projeto.
    - Na seção **Authentication**, ative o provedor de login do **Google**.
    - Na seção **Firestore Database**, crie um banco de dados em modo de teste.

3.  **Configure as APIs e Chaves**
    - No `local.properties` (crie este arquivo na raiz do projeto), adicione suas chaves:
      ```properties
      # Chave de API do Google Cloud para o Geocoder e Calendar
      MAPS_API_KEY=SUA_CHAVE_API_DO_GOOGLE_CLOUD

      # Token de Acesso Público do Mapbox
      MAPBOX_ACCESS_TOKEN=SEU_TOKEN_DE_ACESSO_DO_MAPBOX
      ```
    - Ative as seguintes APIs no seu [Google Cloud Console](https://console.cloud.google.com/apis/library):
      - Google Calendar API
      - Geocoding API

4.  **Abra no Android Studio**
    - Abra o projeto no Android Studio, aguarde a sincronização do Gradle e execute o aplicativo.

## Tecnologias e APIs Utilizadas

- **Linguagem**: Java
- **Banco de Dados**: Cloud Firestore
- **Autenticação**: Firebase Authentication com Google Sign-In
- **Mapas**: Mapbox Maps SDK for Android
- **Agenda**: Google Calendar API
- **Localização**: Google Play Services for Location e Geocoder API
