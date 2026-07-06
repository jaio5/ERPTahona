; ============================================================================
;  ERP Tahona - Instalador de escritorio (Inno Setup 6)
;
;  Empaqueta la carpeta autocontenida (dist\erp-desktop, generada por
;  empaquetar-cliente.ps1) en un unico ERP-Tahona-Setup.exe que:
;    - Explica al cliente como funciona (paginas del asistente).
;    - Instala la app sin permisos de administrador (por usuario).
;    - Detecta Docker Desktop y ofrece descargarlo si falta.
;    - Crea un icono de escritorio "ERP Tahona" que arranca Docker, levanta la
;      aplicacion y abre el navegador cuando esta lista (via iniciar-erp.cmd).
;
;  Normalmente se compila con deploy\desktop\build-installer.ps1, que pasa las
;  rutas y la version por linea de comandos. Los valores por defecto permiten
;  compilarlo tambien abriendo este .iss directamente en Inno Setup.
; ============================================================================

#ifndef MyAppVersion
  #define MyAppVersion "1.0.0"
#endif
#ifndef MyPackageDir
  ; Carpeta con el paquete del cliente, relativa a este .iss
  #define MyPackageDir "..\..\..\dist\erp-desktop"
#endif
#ifndef MyOutputDir
  #define MyOutputDir "..\..\..\dist"
#endif

#define MyAppName "ERP Tahona"
#define MyPublisher "Panaderia Tahona"
#define MyStartCmd "iniciar-erp.cmd"
#define MyStopCmd "parar-erp.cmd"

[Setup]
AppId={{EAC30433-B13B-4841-8DEC-761426489A91}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyPublisher}
DefaultDirName={autopf}\ERP Tahona
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
DisableDirPage=no
; Instalacion por usuario: no pide administrador y la carpeta es escribible
; (el lanzador guarda ahi el .env y los certificados).
PrivilegesRequired=lowest
OutputDir={#MyOutputDir}
OutputBaseFilename=ERP-Tahona-Setup-{#MyAppVersion}
SetupIconFile=erp-icono.ico
UninstallDisplayIcon={app}\erp-icono.ico
UninstallDisplayName={#MyAppName}
WizardStyle=modern
Compression=lzma2/max
SolidCompression=yes
MinVersion=10.0
InfoBeforeFile=como-funciona.txt
AllowNoIcons=yes

[Languages]
Name: "es"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear un icono en el &escritorio"; GroupDescription: "Accesos directos:"

[Files]
; Paquete autocontenido del cliente (compose, lanzadores, Caddyfile, certs, imagen .tar)
Source: "{#MyPackageDir}\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs ignoreversion
; Icono de la aplicacion (para los accesos directos)
Source: "erp-icono.ico"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Icono de escritorio -> arranca Docker + app + navegador (iniciar-erp.cmd)
Name: "{autodesktop}\ERP Tahona"; Filename: "{app}\{#MyStartCmd}"; WorkingDir: "{app}"; IconFilename: "{app}\erp-icono.ico"; Comment: "Arrancar ERP Tahona y abrir la aplicacion"; Tasks: desktopicon
; Menu Inicio
Name: "{group}\ERP Tahona"; Filename: "{app}\{#MyStartCmd}"; WorkingDir: "{app}"; IconFilename: "{app}\erp-icono.ico"; Comment: "Arrancar ERP Tahona y abrir la aplicacion"
Name: "{group}\Detener ERP Tahona"; Filename: "{app}\{#MyStopCmd}"; WorkingDir: "{app}"; IconFilename: "{app}\erp-icono.ico"; Comment: "Detener la aplicacion (los datos se conservan)"
Name: "{group}\Como funciona (LEEME)"; Filename: "{app}\LEEME.txt"
Name: "{group}\Desinstalar ERP Tahona"; Filename: "{uninstallexe}"

[Run]
; Ofrecer arrancar la aplicacion nada mas instalar
Filename: "{app}\{#MyStartCmd}"; Description: "Iniciar ERP Tahona ahora"; WorkingDir: "{app}"; Flags: postinstall shellexec skipifsilent nowait

[Messages]
es.BeveledLabel=ERP Tahona

[Code]
{ --- Deteccion de Docker Desktop y aviso amable si falta --- }

function DockerInstalado(): Boolean;
begin
  Result :=
    FileExists(ExpandConstant('{commonpf}\Docker\Docker\Docker Desktop.exe')) or
    FileExists(ExpandConstant('{commonpf64}\Docker\Docker\Docker Desktop.exe')) or
    RegKeyExists(HKLM, 'SOFTWARE\Docker Inc.\Docker Desktop') or
    RegKeyExists(HKCU, 'SOFTWARE\Docker Inc.\Docker Desktop');
end;

procedure InitializeWizard();
var
  ErrorCode: Integer;
begin
  if not DockerInstalado() then
  begin
    if MsgBox(
      'ERP Tahona necesita "Docker Desktop", que no parece estar instalado en este equipo.' + #13#10 + #13#10 +
      'Es un motor gratuito. Puedes instalarlo ahora (recomendado) o continuar y hacerlo despues.' + #13#10 + #13#10 +
      'Sin Docker Desktop, la aplicacion no arrancara.' + #13#10 + #13#10 +
      'Quieres abrir ahora la pagina de descarga de Docker Desktop?',
      mbConfirmation, MB_YESNO) = IDYES then
    begin
      ShellExec('open', 'https://www.docker.com/products/docker-desktop',
                '', '', SW_SHOWNORMAL, ewNoWait, ErrorCode);
    end;
  end;
end;
