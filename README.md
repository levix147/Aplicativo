# GoPlan - Aplicativo de Gerenciamento de Eventos para Android

GoPlan é um aplicativo Android desenvolvido em Java que funciona como um sistema de gerenciamento de tarefas e eventos. O projeto demonstra a integração com várias APIs modernas, como Firebase, Mapbox e Google Calendar, seguindo as melhores práticas de desenvolvimento Android.

## Funcionalidades Principais

- **Autenticação de Usuários**: Login e cadastro simplificados utilizando contas Google (via Firebase Authentication).
- **Banco de Dados em Tempo Real**: Os eventos são salvos e sincronizados instantaneamente com o Cloud Firestore.
- **Lista de Eventos com Busca**: Visualização de todos os eventos em uma lista eficiente com funcionalidade de busca por nome.
- **Detalhes do Evento com Mapa**: Visualização dos detalhes completos de um evento, incluindo um mapa interativo (Mapbox) que mostra a localização.
- **Criação de Eventos Completa**:
    - Seleção de local em um mapa interativo (Mapbox).
    - Seleção de data e hora.
    - Opção de adicionar o evento criado diretamente na agenda do usuário (Google Calendar API).
- **Segurança de Chaves de API**: Todas as chaves e tokens são mantidos fora do controle de versão para máxima segurança.

## 🛠️ Como Configurar e Executar o Projeto

Para compilar e executar este projeto, você precisará fornecer seus próprios arquivos de configuração e chaves de API. Siga os passos abaixo.

### 1. Configure o Firebase

- Vá para o [Firebase Console](https://console.firebase.google.com/) e crie um novo projeto.
- Adicione um aplicativo Android ao seu projeto com o nome de pacote: `com.example.goplan`.
- Baixe o arquivo `google-services.json` gerado.
- **Coloque o arquivo `google-services.json` na pasta `app/` do projeto.**
- No console do Firebase, habilite os seguintes serviços:
    - **Authentication**: Ative o provedor de login **Google**.
    - **Firestore Database**: Crie um novo banco de dados (pode ser em modo de teste).

### 2. Configure as Chaves de API e Tokens

- Na **raiz do projeto**, crie um arquivo chamado `local.properties`.
- Adicione suas chaves e tokens a este arquivo, no seguinte formato:

  ```properties
  # Chave de API do Google Cloud. Usada para Geocoding e Google Calendar API.
  MAPS_API_KEY=SUA_CHAVE_API_DO_GOOGLE_CLOUD

  # Token de Acesso Público do Mapbox. Usado para os mapas.
  MAPBOX_ACCESS_TOKEN=SEU_TOKEN_DE_ACESSO_DO_MAPBOX
  ```

- **Obtenha as chaves:**
    - `MAPS_API_KEY`: Crie no [Google Cloud Console](https://console.cloud.google.com/apis/credentials). Lembre-se de ativar as APIs **Geocoding API** e **Google Calendar API**.
    - `MAPBOX_ACCESS_TOKEN`: Crie uma conta no [Mapbox](https://www.mapbox.com/) e copie seu "Default public token".

### 3. Compile e Execute

- Abra o projeto no Android Studio.
- Sincronize o projeto com os arquivos Gradle (Sync Project with Gradle Files).
- Compile e execute o aplicativo em um emulador ou dispositivo físico.

## Tecnologias Utilizadas

- **Linguagem**: Java
- **Arquitetura**: Android SDK nativo
- **Banco de Dados**: Cloud Firestore
- **Autenticação**: Firebase Authentication
- **Mapas**: Mapbox Maps SDK for Android
- **Agenda**: Google Calendar API
- **Localização**: Google Play Services & Geocoder API
