document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".login-form");

    // Verifica se está na página de login
    if (form) {
        console.log("✅ Página de Login detectada.");

        form.addEventListener("submit", async (event) => {
            event.preventDefault();

            const username = document.getElementById("username")?.value.trim();
            const password = document.getElementById("password")?.value.trim();

            if (!username || !password) {
                alert("Por favor, preencha usuário e senha.");
                return;
            }

            try {
                const response = await fetch("/login", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                    },
                    credentials: "include",
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
                });

                if (response.status === 200) {
                    alert("✅ Login bem-sucedido!");
                    window.location.href = "/static/dashboard.html";
                } else {
                    const errorMessage = await response.text();
                    alert(errorMessage || "Usuário ou senha inválidos.");
                }
            } catch (error) {
                console.error("❌ Erro na requisição:", error);
                alert("Erro ao tentar se conectar ao servidor.");
            }
        });
    } else {
        console.log("🔄 Página de Dashboard detectada.");

        const conferentes = []; // Mover a variável conferentes para o escopo global
        const tabelaBody = document.querySelector("#tabelaConferentes tbody");
        const conferenteButton = document.getElementById("btnConferente");
        const conferenteSection = document.getElementById("conferenteSection");
        const conferenteInput = document.getElementById("conferenteInput");
        const cadastrarButton = document.getElementById("btnCadastrar");
        const alterarButton = document.getElementById("btnAlterar");
        const excluirButton = document.getElementById("btnExcluir");
        const sairButton = document.getElementById("btnSair");

        // Função para atualizar a tabela de conferentes
        function atualizarTabelaConferentes() {
            tabelaBody.innerHTML = ""; // Limpa a tabela antes de atualizar

            if (conferentes.length === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `<td colspan="3">Nenhum conferente encontrado.</td>`;
                tabelaBody.appendChild(row);
                return;
            }

            conferentes.forEach((conferente) => {
                const row = document.createElement("tr");

                // Decodificar o nome do conferente
                const nomeDecodificado = decodeURIComponent(conferente.name);

                row.innerHTML = `
                    <td>${conferente.id}</td>
                    <td>${nomeDecodificado}</td>
                    <td>
                        <button class="btn btnAlterar" data-id="${conferente.id}">Alterar</button>
                        <button class="btn btnExcluir" data-id="${conferente.id}">Excluir</button>
                    </td>
                `;
                tabelaBody.appendChild(row);
            });

            // Adicionar eventos aos botões de Alterar e Excluir
            document.querySelectorAll(".btnAlterar").forEach(button => {
                button.addEventListener("click", (e) => {
                    const conferenteId = e.target.dataset.id;
                    const conferente = conferentes.find(c => c.id == conferenteId);
                    const novoNome = prompt("Digite o novo nome do conferente:", conferente.name);
                    if (novoNome && novoNome.trim()) {
                        conferente.name = novoNome.trim();
                        atualizarTabelaConferentes(); // Atualiza a tabela
                        alert(`✏️ Conferente alterado para ${novoNome}`);
                    }
                });
            });

            document.querySelectorAll(".btnExcluir").forEach(button => {
                button.addEventListener("click", (e) => {
                    const conferenteId = e.target.dataset.id;
                    const conferenteIndex = conferentes.findIndex(c => c.id == conferenteId);
                    if (conferenteIndex > -1) {
                        const conferenteNome = conferentes[conferenteIndex].name;
                        const confirmDelete = confirm(`Tem certeza que deseja excluir o conferente ${conferenteNome}?`);
                        if (confirmDelete) {
                            conferentes.splice(conferenteIndex, 1); // Exclui o conferente
                            atualizarTabelaConferentes(); // Atualiza a tabela
                            alert(`🗑️ Conferente ${conferenteNome} excluído com sucesso!`);
                        }
                    }
                });
            });
        }

        // Função para carregar conferentes da API
        async function carregarConferentes() {
            try {
                const response = await fetch("/api/conferentes");
                if (response.ok) {
                    const data = await response.json();
                    conferentes.length = 0; // Limpar a lista de conferentes
                    conferentes.push(...data); // Adicionar os conferentes recebidos da API
                    atualizarTabelaConferentes(); // Atualiza a tabela
                } else {
                    alert("Erro ao carregar os conferentes.");
                }
            } catch (error) {
                console.error("❌ Erro ao carregar conferentes:", error);
                alert("Erro ao tentar carregar os conferentes.");
            }
        }

        // Carregar conferentes ao iniciar a página
        carregarConferentes();

        // Evento para exibir/ocultar a seção de Conferentes
        if (conferenteButton) {
            conferenteButton.addEventListener("click", () => {
                // Alterna a visibilidade da seção de conferentes
                conferenteSection.style.display =
                    conferenteSection.style.display === "none" || conferenteSection.style.display === ""
                        ? "block"
                        : "none";
            });
        }

        // Evento do botão de Cadastrar
        if (cadastrarButton) {
            cadastrarButton.addEventListener("click", async () => {
                const conferenteName = conferenteInput.value.trim();
                if (conferenteName) {
                    try {
                        const response = await fetch("/api/conferentes", {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/x-www-form-urlencoded",
                            },
                            body: `name=${encodeURIComponent(conferenteName)}&role=CONFERENTE`
                        });

                        if (response.status === 201) {
                            const conferente = await response.json();
                            conferentes.push(conferente); // Adiciona o conferente à lista
                            atualizarTabelaConferentes(); // Atualiza a tabela
                            conferenteInput.value = ""; // Limpa o campo de input
                            alert(`✅ Conferente ${conferenteName} cadastrado com sucesso!`);
                        } else {
                            const errorMessage = await response.text();
                            alert(errorMessage || "Erro ao cadastrar o conferente.");
                        }
                    } catch (error) {
                        console.error("❌ Erro ao cadastrar conferente:", error);
                        alert("Erro ao tentar cadastrar o conferente.");
                    }
                } else {
                    alert("Por favor, insira o nome do conferente.");
                }
            });
        }

        // Evento de Alterar
        if (alterarButton) {
            alterarButton.addEventListener("click", async () => {
                const conferenteName = conferenteInput.value.trim();
                const conferenteId = document.getElementById("conferenteId").value.trim();

                if (conferenteName && conferenteId) {
                    try {
                        const response = await fetch(`/api/conferentes/${conferenteId}`, {
                            method: "PUT",
                            headers: {
                                "Content-Type": "application/x-www-form-urlencoded",
                            },
                            body: `name=${encodeURIComponent(conferenteName)}&role=CONFERENTE`
                        });

                        if (response.status === 200) {
                            alert(`✏️ Conferente ${conferenteName} alterado com sucesso!`);
                            conferenteInput.value = "";
                            atualizarTabelaConferentes(); // Atualiza a tabela
                        } else {
                            const errorMessage = await response.text();
                            alert(errorMessage || "Erro ao alterar o conferente.");
                        }
                    } catch (error) {
                        console.error("❌ Erro ao alterar conferente:", error);
                        alert("Erro ao tentar alterar o conferente.");
                    }
                } else {
                    alert("Por favor, insira o nome do conferente e selecione um conferente.");
                }
            });
        }

        // Evento de Excluir
        if (excluirButton) {
            excluirButton.addEventListener("click", async () => {
                const conferenteId = document.getElementById("conferenteId").value.trim();

                if (conferenteId) {
                    const confirmDelete = confirm(`❓ Tem certeza de que deseja excluir o conferente de ID ${conferenteId}?`);
                    if (confirmDelete) {
                        try {
                            const response = await fetch(`/api/conferentes/${conferenteId}`, {
                                method: "DELETE",
                            });

                            if (response.status === 200) {
                                alert(`🗑️ Conferente excluído com sucesso!`);
                                // Atualiza a tabela
                                const conferenteIndex = conferentes.findIndex(c => c.id == conferenteId);
                                if (conferenteIndex > -1) conferentes.splice(conferenteIndex, 1);
                                atualizarTabelaConferentes(); // Atualiza a tabela
                            } else {
                                const errorMessage = await response.text();
                                alert(errorMessage || "Erro ao excluir o conferente.");
                            }
                        } catch (error) {
                            console.error("❌ Erro ao excluir conferente:", error);
                            alert("Erro ao tentar excluir o conferente.");
                        }
                    }
                } else {
                    alert("Por favor, selecione um conferente para excluir.");
                }
            });
        }

        // Evento do botão de Sair
        if (sairButton) {
            sairButton.addEventListener("click", async () => {
                try {
                    const response = await fetch("/logout", {
                        method: "POST",
                        credentials: "include",
                    });

                    if (response.ok) {
                        window.location.href = "/static/login.html";
                    } else {
                        console.error("❌ Falha no logout", response);
                        alert("Erro ao tentar sair.");
                    }
                } catch (error) {
                    console.error("❌ Erro ao fazer logout", error);
                    alert("Erro ao tentar sair.");
                }
            });
        }
    }
});
