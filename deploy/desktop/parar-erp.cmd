@echo off
REM Doble clic para detener ERP Tahona (conserva los datos).
title ERP Tahona - Detener
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0parar-erp.ps1"
