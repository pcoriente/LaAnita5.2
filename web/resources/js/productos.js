                    function handleUnidadProducto(xhr, status, args) {
                        if(args.validationFailed || !args.okUnidadProducto) {
                            mttoUnidadesProductoDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoUnidadesProductoDlg.hide();
                        }
                    }
                    
                    function handleSubGrupo(xhr, status, args) {
                        if(args.validationFailed || !args.okSubGrupo) {
                            mttoSubGruposDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoSubGruposDlg.hide();
                        }
                    }
                    
                    function handleGrupo(xhr, status, args) {
                        if(args.validationFailed || !args.okGrupo) {
                            mttoGruposDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoGruposDlg.hide();
                        }
                    }
                    
                    function handleUnidadEmpaque(xhr, status, args) {
                        if(args.validationFailed || !args.okUnidadEmpaque) {
                            mttoUnidadesEmpaqueDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoUnidadesEmpaqueDlg.hide();
                        }
                    }
                    
                    function handleMarca(xhr, status, args) {
                        if(args.validationFailed || !args.okMarca) {
                            mttoMarcasDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoMarcasDlg.hide();
                        }
                    }
                    
                    function handleBuscar(xhr, status, args) {
                        if(!(args.validationFailed || !args.okBuscar)) {
                            buscarProductoDlg.hide();
                        }
                    }
                    
                    function handleProducto(xhr, status, args) {
                        if(args.validationFailed || !args.okProducto) {
                            mttoProductoDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoProductoDlg.hide();
                        }
                    }
                    
                    function handleUpc(xhr, status, args) {
                        if(args.validationFailed || !args.okUpc) {
                            mttoUpcDlg.jq.effect("shake", { times: 5}, 100);
                        } else {
                            mttoUpcDlg.hide();
                        }
                    }


