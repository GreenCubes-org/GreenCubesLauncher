; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "GreenCubes"
!define PRODUCT_PUBLISHER "GreenCubes"
!define PRODUCT_VERSION ""
!define PRODUCT_WEB_SITE "https://greencubes.org"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\greencubes.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

SetCompressor lzma

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "C:\_Greencubes\Git\GreenCubesLauncher\install\icon.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Language Selection Dialog Settings
!define MUI_LANGDLL_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
;!insertmacro MUI_PAGE_LICENSE "c:\path\to\licence\YourSoftwareLicence.txt"
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\greencubes.exe"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Ukrainian"

; Reserve files
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "C:\_Greencubes\Git\GreenCubesLauncher\install\greencubes-setup.exe"
InstallDir "$PROGRAMFILES\GreenCubes"
ShowInstDetails show

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  inetc::get /RESUME "" /QUESTION "" "https://greencubes.org/client/windows-installer.zip" "package.zip"
    Pop $0
    StrCmp $0 "OK" dlok
    MessageBox MB_OK|MB_ICONEXCLAMATION "http upload Error, click OK to abort installation" /SD IDOK
    Abort
  dlok:
  ;CreateDirectory "$SMPROGRAMS\GreenCubes"
  ;CreateShortCut "$SMPROGRAMS\GreenCubes\GreenCubes.lnk" "$INSTDIR\greencubes.exe"
  ;CreateShortCut "$DESKTOP\GreenCubes.lnk" "$INSTDIR\greencubes.exe"
SectionEnd