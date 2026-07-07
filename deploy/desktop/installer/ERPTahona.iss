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
{ ------------------------------------------------------------------------- }
{  Deteccion de Docker Desktop + asistente de configuracion de VeriFactu     }
{ ------------------------------------------------------------------------- }

var
  PageVfMode: TInputOptionWizardPage;   { modo: no configurar / pruebas / produccion }
  PageVfCert: TInputFileWizardPage;     { fichero .p12 del certificado }
  PageVfData: TInputQueryWizardPage;    { contrasena, alias, endpoint produccion }

function DockerInstalado(): Boolean;
begin
  Result :=
    FileExists(ExpandConstant('{commonpf}\Docker\Docker\Docker Desktop.exe')) or
    FileExists(ExpandConstant('{commonpf64}\Docker\Docker\Docker Desktop.exe')) or
    RegKeyExists(HKLM, 'SOFTWARE\Docker Inc.\Docker Desktop') or
    RegKeyExists(HKCU, 'SOFTWARE\Docker Inc.\Docker Desktop');
end;

function VfConfigurar(): Boolean;
begin
  { El indice 0 es "no configurar ahora" }
  Result := (PageVfMode.SelectedValueIndex > 0);
end;

function VfEsProduccion(): Boolean;
begin
  Result := (PageVfMode.SelectedValueIndex = 2);
end;

procedure InitializeWizard();
var
  ErrorCode: Integer;
begin
  { --- Aviso amable si falta Docker Desktop --- }
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

  { --- Pagina 1: elegir modo de VeriFactu --- }
  PageVfMode := CreateInputOptionPage(wpInfoBefore,
    'Facturacion VeriFactu',
    'Como quieres dejar configurada la facturacion electronica',
    'La aplicacion funciona en cualquier caso. Para firmar facturas y (opcionalmente)' + #13#10 +
    'remitirlas a la AEAT necesitas el certificado de la empresa. Podras cambiarlo despues.',
    True, False);
  PageVfMode.Add('No configurar ahora (lo hare mas tarde con mi gestor)');
  PageVfMode.Add('Tengo certificado - modo PRUEBAS (firma y QR, sin remision real; recomendado para empezar)');
  PageVfMode.Add('Tengo certificado - modo PRODUCCION (remision real a la AEAT)');
  PageVfMode.SelectedValueIndex := 0;

  { --- Pagina 2: seleccionar el certificado .p12 --- }
  PageVfCert := CreateInputFilePage(PageVfMode.ID,
    'Certificado de la empresa',
    'Selecciona el certificado digital (.p12 o .pfx)',
    'Se copiara dentro de la instalacion (carpeta certs) y se usara para firmar las facturas.');
  PageVfCert.Add('Fichero de certificado:',
    'Certificados (*.p12;*.pfx)|*.p12;*.pfx|Todos los ficheros (*.*)|*.*', '.p12');

  { --- Pagina 3: contrasena, alias y (si produccion) endpoint --- }
  PageVfData := CreateInputQueryPage(PageVfCert.ID,
    'Datos del certificado',
    'Contrasena, alias y servicio de la AEAT',
    'Introduce la contrasena del certificado. El alias suele poder dejarse por defecto.' + #13#10 +
    'La URL de la AEAT solo hace falta en modo produccion (te la facilita tu gestor).');
  PageVfData.Add('Contrasena del certificado:', True);
  PageVfData.Add('Alias de la clave:', False);
  PageVfData.Add('URL del servicio AEAT (solo modo PRODUCCION):', False);
  PageVfData.Values[1] := 'mi_certificado';
end;

function ShouldSkipPage(PageID: Integer): Boolean;
begin
  Result := False;
  if (PageID = PageVfCert.ID) or (PageID = PageVfData.ID) then
    Result := not VfConfigurar();
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;

  if CurPageID = PageVfCert.ID then
  begin
    if Trim(PageVfCert.Values[0]) = '' then
    begin
      MsgBox('Selecciona el fichero del certificado (.p12 o .pfx).', mbError, MB_OK);
      Result := False;
    end
    else if not FileExists(PageVfCert.Values[0]) then
    begin
      MsgBox('No se encuentra el fichero seleccionado. Revisa la ruta.', mbError, MB_OK);
      Result := False;
    end;
  end

  else if CurPageID = PageVfData.ID then
  begin
    if Trim(PageVfData.Values[0]) = '' then
    begin
      MsgBox('Introduce la contrasena del certificado.', mbError, MB_OK);
      Result := False;
    end
    else if VfEsProduccion() then
    begin
      if Trim(PageVfData.Values[2]) = '' then
      begin
        MsgBox('En modo PRODUCCION debes indicar la URL del servicio de la AEAT.' + #13#10 +
               'Te la facilita tu gestor o la propia AEAT. Si aun no la tienes, usa el modo PRUEBAS.',
               mbError, MB_OK);
        Result := False;
      end
      else if Pos('prewww', Lowercase(PageVfData.Values[2])) > 0 then
      begin
        MsgBox('Esa URL es del entorno de PRUEBAS de la AEAT (contiene "prewww").' + #13#10 +
               'En produccion debe ser la URL real; la aplicacion se niega a arrancar' + #13#10 +
               'con remision activa apuntando a pruebas.', mbError, MB_OK);
        Result := False;
      end
      else if Pos('agenciatributaria.gob.es', Lowercase(PageVfData.Values[2])) = 0 then
      begin
        if MsgBox('La URL no parece la oficial de la AEAT (no contiene "agenciatributaria.gob.es").' + #13#10 + #13#10 +
                  'La de produccion suele ser:' + #13#10 +
                  '  https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP' + #13#10 +
                  '  (o www10 si usas certificado de sello).' + #13#10 + #13#10 +
                  'Quieres continuar con la URL que has escrito de todas formas?',
                  mbConfirmation, MB_YESNO) = IDNO then
          Result := False;
      end;
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  conf: TStringList;
  certName, certDest, confPath: String;
begin
  if CurStep <> ssPostInstall then Exit;

  confPath := ExpandConstant('{app}\verifactu.conf');

  if not VfConfigurar() then
  begin
    { Si se reinstala eligiendo "no configurar", no dejar un conf antiguo activo }
    if FileExists(confPath) then DeleteFile(confPath);
    Exit;
  end;

  { Copiar el certificado a la carpeta certs de la instalacion }
  certName := ExtractFileName(PageVfCert.Values[0]);
  ForceDirectories(ExpandConstant('{app}\certs'));
  certDest := ExpandConstant('{app}\certs\') + certName;
  if not CopyFile(PageVfCert.Values[0], certDest, False) then
  begin
    MsgBox('No se pudo copiar el certificado a la instalacion. Podras configurarlo despues.', mbError, MB_OK);
    Exit;
  end;

  { Escribir verifactu.conf (lo aplica iniciar-erp.ps1 al generar el .env) }
  conf := TStringList.Create;
  try
    conf.Add('# Generado por el instalador de ERP Tahona. Lo aplica iniciar-erp.ps1.');
    if VfEsProduccion() then conf.Add('MODE=produccion') else conf.Add('MODE=pruebas');
    conf.Add('CERT_FILE=' + certName);
    conf.Add('CERT_PASSWORD=' + PageVfData.Values[0]);
    if Trim(PageVfData.Values[1]) <> '' then
      conf.Add('KEY_ALIAS=' + Trim(PageVfData.Values[1]))
    else
      conf.Add('KEY_ALIAS=mi_certificado');
    if VfEsProduccion() then
      conf.Add('AEAT_ENDPOINT=' + Trim(PageVfData.Values[2]));
    conf.SaveToFile(confPath);
  finally
    conf.Free;
  end;
end;
