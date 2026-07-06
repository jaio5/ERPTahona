@echo off
REM Doble clic para arrancar ERP Tahona.
title ERP Tahona - Iniciar
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar-erp.ps1"
if errorlevel 1 pause
