document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".login-form");
    const conferenteInput = document.getElementById("conferenteInput");
    const tabelaBody = document.querySelector("#tabelaConferentes tbody");
    const conferenteButton = document.getElementById("btnConferente");
    const conferenteSection = document.getElementById("conferenteSection");
    const historicoButton = document.getElementById("btnHistorico");
    const historicoSection = document.getElementById("historicoSection");
    const lancarButton = document.getElementById("btnLancar");
    const lancarSection = document.getElementById("lancarSection");
    const cadastrarButton = document.getElementById("btnCadastrar");
    const sairButton = document.getElementById("btnSair");
    const selectConferente = document.getElementById("novaConferente");
    const lancarForm = document.getElementById("lancarForm");
    const consultaInput = document.getElementById("consultaInput");
    const filtroSelect = document.getElementById("filtroSelect");
    const consultarBtn = document.getElementById("consultarBtn");

    // Função para esconder todas as seções
    const esconderTodasSecoes = () => {
        const secoes = [conferenteSection, historicoSection, lancarSection];
        secoes.forEach(secao => {
            if (secao) {
                secao.style.display = "none";
            }
        });
    };

    // Função para exibir uma seção específica
    const exibirSecao = (secao) => {
        esconderTodasSecoes();
        if (secao) {
            secao.style.display = "block";
        }
    };

    // Esconde todas as seções inicialmente
    esconderTodasSecoes();

    // Função para exibir mensagens de alerta
    const exibirAlerta = (mensagem, tipo = "info") => {
        if (mensagem !== "Conferentes carregados com sucesso!" && mensagem !== "Conferente atualizado com sucesso!") {
            alert(`${tipo === "error" ? "❌" : "✅"} ${mensagem}`);
        }
    };

    // Função para manipular a resposta da API
    const tratarResposta = async (response, sucessoMsg, erroMsg) => {
        if (response.ok) {
            exibirAlerta(sucessoMsg);
            return await response.text();
        } else {
            const errorMessage = await response.text();
            exibirAlerta(errorMessage || erroMsg, "error");
            throw new Error(errorMessage || erroMsg);
        }
    };

    // Função para realizar o login
    const login = async (username, password) => {
        try {
            const response = await fetch("/login", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                credentials: "include",
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
            });

            await tratarResposta(response, "Login bem-sucedido!", "Usuário ou senha inválidos.");
            window.location.href = "/static/dashboard.html";
        } catch (error) {
            console.error("❌ Erro na requisição:", error);
            exibirAlerta("Erro ao tentar se conectar ao servidor.", "error");
        }
    };

    // Função para carregar os conferentes e popular o select
    const carregarConferentesNoSelect = async () => {
        try {
            const response = await fetch("/api/conferentes");
            if (!response.ok) throw new Error("Erro ao carregar conferentes.");

            const conferentes = await response.json();
            selectConferente.innerHTML = ""; // Limpar antes de preencher

            if (conferentes.length === 0) {
                selectConferente.innerHTML = "<option value=''>Nenhum conferente encontrado</option>";
                return;
            }

            conferentes.forEach(conferente => {
                const option = document.createElement("option");
                option.value = conferente.id;
                option.textContent = decodeURIComponent(conferente.name);
                selectConferente.appendChild(option);
            });

        } catch (error) {
            console.error("❌ Erro ao carregar conferentes no select:", error);
            alert("Erro ao carregar conferentes.");
        }
    };

    // Exibir a seção de lançamento e carregar conferentes no select
    if (lancarButton && lancarSection) {
        lancarButton.addEventListener("click", async () => {
            exibirSecao(lancarSection);
            await carregarConferentesNoSelect();
        });
    }

    // Função para enviar o formulário de lançamento de conferência
    if (lancarForm) {
        lancarForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const transacao = document.getElementById("novaTransacao").value.trim();
            const notaFiscal = document.getElementById("novaNotaFiscal").value.trim();
            const conferenteId = selectConferente.value;
            const tipo = document.getElementById("novaTipo").value;

            if (!transacao || !notaFiscal || !conferenteId || !tipo) {
                alert("Preencha todos os campos para lançar a conferência.");
                return;
            }

            try {
                const response = await fetch("/api/registros_conferencia", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `transacao=${encodeURIComponent(transacao)}&nota_fiscal=${encodeURIComponent(notaFiscal)}&conferente_id=${conferenteId}&tipo=${tipo}`
                });

                if (!response.ok) {
                    const errorMsg = await response.text();
                    throw new Error(errorMsg || "Erro ao lançar a conferência.");
                }

                alert("✅ Conferência lançada com sucesso!");
                lancarForm.reset(); // Limpar o formulário após sucesso
            } catch (error) {
                console.error("❌ Erro ao lançar conferência:", error);
                alert(error.message);
            }
        });
    }

    // Função para carregar conferentes
    const carregarConferentes = async () => {
        try {
            const response = await fetch("/api/conferentes");
            if (!response.ok) throw new Error("Erro ao carregar conferentes.");

            const data = await response.json();

            console.log("📊 Conferentes carregados:", data);
            return Array.isArray(data) ? data : [];
        } catch (error) {
            console.error("❌ Erro ao carregar conferentes:", error);
            exibirAlerta("Erro ao tentar carregar os conferentes.", "error");
            return [];
        }
    };

    // Função para atualizar a tabela de conferentes
    const atualizarTabelaConferentes = (conferentes) => {
        tabelaBody.innerHTML = conferentes.length === 0
            ? "<tr><td colspan='3'>Nenhum conferente encontrado.</td></tr>"
            : conferentes.map(conferente => ` 
                <tr> 
                    <td>${conferente.id}</td>
                    <td>${decodeURIComponent(conferente.name)}</td>
                    <td>
                        <button class="btn btnAlterar" data-id="${conferente.id}">Alterar</button>
                        <button class="btn btnExcluir" data-id="${conferente.id}">Excluir</button>
                    </td>
                </tr>`).join('');
    };

    // Função para realizar o cadastro de um conferente
    const cadastrarConferente = async (conferenteName) => {
        try {
            const response = await fetch("/api/conferentes", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `name=${conferenteName}&role=CONFERENTE`
            });

            await tratarResposta(response, `Conferente ${conferenteName} cadastrado com sucesso!`, "Erro ao cadastrar o conferente.");
            conferenteInput.value = "";
        } catch (error) {
            console.error("❌ Erro ao cadastrar conferente:", error);
            exibirAlerta("Erro ao tentar cadastrar o conferente.", "error");
        }
    };

    // Função para realizar logout
    const logout = async () => {
        try {
            const response = await fetch("/logout", { method: "POST", credentials: "include" });
            await tratarResposta(response, "Você saiu com sucesso!", "Erro ao tentar sair.");
            window.location.href = "/static/login.html";
        } catch (error) {
            console.error("❌ Erro ao fazer logout", error);
            exibirAlerta("Erro ao tentar sair.", "error");
        }
    };

    // Inicializa a página de login
    if (form) {
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            const username = document.getElementById("username").value.trim();
            const password = document.getElementById("password").value.trim();
            if (username && password) {
                login(username, password);
            } else {
                exibirAlerta("Por favor, preencha usuário e senha.", "error");
            }
        });
    }

    // Página de Dashboard
    if (!form) {
        // Toggle para abrir a seção de conferentes
        if (conferenteButton && conferenteSection) {
            conferenteButton.addEventListener("click", async () => {
                exibirSecao(conferenteSection);
                const conferentes = await carregarConferentes();
                atualizarTabelaConferentes(conferentes);
            });
        }

        // Toggle para abrir a seção de lançar conferência
        if (lancarButton && lancarSection) {
            lancarButton.addEventListener("click", () => {
                exibirSecao(lancarSection);
            });
        }

        // Cadastrar conferente
        if (cadastrarButton) {
            cadastrarButton.addEventListener("click", async () => {
                const conferenteName = conferenteInput.value.trim();
                if (conferenteName) {
                    await cadastrarConferente(conferenteName);
                    const conferentes = await carregarConferentes();
                    atualizarTabelaConferentes(conferentes);
                } else {
                    exibirAlerta("Por favor, insira o nome do conferente.", "error");
                }
            });
        }

        // Manipular eventos de alterar e excluir conferentes
        if (tabelaBody) {
            tabelaBody.addEventListener("click", async (event) => {
                const target = event.target;
                const id = target.dataset.id;
                if (!id) return;

                if (target.classList.contains("btnAlterar")) {
                    const novoNome = prompt("Digite o novo nome do conferente:");
                    if (novoNome) {
                        try {
                            const response = await fetch(`/api/conferentes/${id}`, {
                                method: "PUT",
                                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                                body: `name=${novoNome}&role=CONFERENTE`
                            });

                            await tratarResposta(response, "Conferente atualizado com sucesso!", "Erro ao alterar o conferente.");
                            const conferentes = await carregarConferentes();
                            atualizarTabelaConferentes(conferentes);
                        } catch (error) {
                            console.error("❌ Erro ao alterar conferente:", error);
                        }
                    }
                }

                if (target.classList.contains("btnExcluir")) {
                    if (confirm("Tem certeza que deseja excluir este conferente?")) {
                        try {
                            const response = await fetch(`/api/conferentes/${id}`, { method: "DELETE" });
                            await tratarResposta(response, "Conferente excluído com sucesso!", "Erro ao excluir o conferente.");
                            const conferentes = await carregarConferentes();
                            atualizarTabelaConferentes(conferentes);
                        } catch (error) {
                            console.error("❌ Erro ao excluir conferente:", error);
                        }
                    }
                }
            });
        }

        // Função para carregar o histórico com ou sem filtro
        const carregarHistorico = async (filtro = "", valor = "") => {
            try {
                let url = "/api/registros_conferencia";

                // Ajustar a URL com base no filtro e valor (transacao ou notaFiscal)
                if (filtro && valor) {
                    if (filtro === "transacao") {
                        url += `/transacao/${encodeURIComponent(valor)}`;
                    } else if (filtro === "nota_fiscal") {
                        url += `/notafiscal/${encodeURIComponent(valor)}`;
                    } else {
                        throw new Error("Filtro inválido.");
                    }
                }

                console.log("🔍 Consultando histórico com URL:", url);

                const response = await fetch(url);
                console.log("Response Status:", response.status);

                // Armazenar a resposta em JSON diretamente
                const historico = await response.json();
                console.log("📊 Dados recebidos da API:", historico);

                if (!response.ok) throw new Error("Erro ao carregar histórico.");

                const historicoTableBody = document.querySelector("#historicoTable tbody");

                // Limpar a tabela antes de preencher
                historicoTableBody.innerHTML = "";

                if (historico.length === 0) {
                    historicoTableBody.innerHTML = "<tr><td colspan='5'>Nenhum registro encontrado.</td></tr>";
                    return;
                }

                historico.forEach(item => {
                    const row = document.createElement("tr");

                    // Garantir que a data seja exibida corretamente
                    const data = new Date(item.data + "T00:00:00");
                    const dataFormatada = isNaN(data) ? 'Data inválida' : data.toLocaleDateString('pt-BR');

                    row.innerHTML = `
            <td>${dataFormatada}</td>
            <td>${item.tipo}</td>
            <td>${item.nota_fiscal}</td>
            <td>${item.transacao}</td>
            <td>${item.conferente}</td>
        `;
                    historicoTableBody.appendChild(row);
                });

                console.log("✅ Histórico atualizado com sucesso!");
            } catch (error) {
                console.error("❌ Erro ao carregar histórico:", error);
                exibirAlerta("Erro ao carregar histórico.", "error");
            }
        };

        // Adicionar evento ao botão de consulta
        if (consultarBtn) {
            consultarBtn.addEventListener("click", async () => {
                const filtro = filtroSelect.value; // "transacao" ou "nota_fiscal"
                const valor = consultaInput.value.trim();

                if (!valor) {
                    exibirAlerta("Digite um termo para realizar a consulta.", "error");
                    return;
                }

                // Garantir que o filtro é compatível com a API
                if (filtro !== "transacao" && filtro !== "nota_fiscal") {
                    exibirAlerta("Selecione um filtro válido.", "error");
                    return;
                }

                console.log(`🔍 Consultando por ${filtro}: ${valor}`);

                // Chamar a função com os parâmetros corretos
                await carregarHistorico(filtro, valor);
            });
        }

        // Carregar histórico completo ao abrir a seção
        if (historicoButton && historicoSection) {
            historicoButton.addEventListener("click", async () => {
                exibirSecao(historicoSection);
                await carregarHistorico(); // Carregar todos os registros
            });
        }

        // Evento de logout
        if (sairButton) {
            sairButton.addEventListener("click", () => {
                if (confirm("Tem certeza que deseja sair?")) {
                    logout();
                }
            });
        }
    }
});