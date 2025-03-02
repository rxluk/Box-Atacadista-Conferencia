document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".login-form");
    const conferenteInput = document.getElementById("conferenteInput");
    const cadastrarButton = document.getElementById("btnCadastrar");
    const tabelaBody = document.querySelector("#tabelaConferentes tbody");

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
        const sairButton = document.getElementById("btnSair");

        if (sairButton) {
            sairButton.addEventListener("click", async () => {
                const confirmar = confirm("🔒 Tem certeza que deseja sair?");
                if (!confirmar) return;

                try {
                    const response = await fetch("/logout", {
                        method: "POST",
                        credentials: "include",
                    });

                    if (response.ok) {
                        alert("👋 Você saiu com sucesso!");
                        window.location.href = "/static/login.html"; // Redireciona para a página de login
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

        if (conferenteButton && conferenteSection) {
            conferenteButton.addEventListener("click", () => {
                // Alterna a visibilidade da seção
                conferenteSection.style.display =
                    conferenteSection.style.display === "none" || conferenteSection.style.display === ""
                        ? "block"
                        : "none";
            });
        }

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
                row.innerHTML = `
                <td>${conferente.id}</td>
                <td>${decodeURIComponent(conferente.name)}</td>
                <td>
                    <button class="btn btnAlterar" data-id="${conferente.id}">Alterar</button>
                    <button class="btn btnExcluir" data-id="${conferente.id}">Excluir</button>
                </td>
            `;
                tabelaBody.appendChild(row);
            });
        }

        // ✅ Atualiza a tabela de conferentes dinamicamente
        function atualizarTabelaConferentes() {
            tabelaBody.innerHTML = "";

            if (conferentes.length === 0) {
                tabelaBody.innerHTML = `<tr><td colspan="3">Nenhum conferente encontrado.</td></tr>`;
                return;
            }

            conferentes.forEach((conferente) => {
                const row = document.createElement("tr");
                row.innerHTML = `
                <td>${conferente.id}</td>
                <td>${conferente.name}</td>
                <td>
                    <button class="btn btnAlterar" data-id="${conferente.id}">Alterar</button>
                    <button class="btn btnExcluir" data-id="${conferente.id}">Excluir</button>
                </td>
            `;
                tabelaBody.appendChild(row);
            });
        }

        // ✅ Cadastra um novo conferente
        if (cadastrarButton) {
            cadastrarButton.addEventListener("click", async () => {
                const conferenteName = conferenteInput.value.trim();
                if (!conferenteName) {
                    alert("Por favor, insira o nome do conferente.");
                    return;
                }

                try {
                    const response = await fetch("/api/conferentes", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded",
                        },
                        body: `name=${conferenteName}&role=CONFERENTE`
                    });

                    const responseText = await response.text();  // Captura a resposta como texto

                    if (response.ok) {
                        alert(`✅ ${responseText}`);  // Exibe a mensagem recebida do backend
                        conferenteInput.value = "";
                        carregarConferentes();

                    } else {
                        alert(`❌ Erro: ${responseText}`);  // Exibe a mensagem de erro
                    }
                } catch (error) {
                    console.error("❌ Erro ao cadastrar conferente:", error);
                    alert("Erro ao tentar cadastrar o conferente.");
                }
            });
        }

        // ✅ Captura eventos de Alterar e Excluir usando delegação de eventos
        tabelaBody.addEventListener("click", async (event) => {
            const target = event.target;
            const id = target.dataset.id;

            if (!id) return;

            // 📝 Alterar conferente
            if (target.classList.contains("btnAlterar")) {
                const novoNome = prompt("Digite o novo nome do conferente:");
                if (!novoNome) return;

                try {
                    const response = await fetch(`/api/conferentes/${id}`, {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded",
                        },
                        body: `name=${novoNome}&role=CONFERENTE`
                    });

                    if (response.ok) {
                        alert("✏️ Conferente atualizado com sucesso!");

                        const index = conferentes.findIndex((c) => c.id == id);
                        if (index !== -1) {
                            conferentes[index].name = novoNome;
                            atualizarTabelaConferentes();
                        }
                    } else {
                        const errorMessage = await response.text();
                        alert(errorMessage || "Erro ao alterar o conferente.");
                    }
                } catch (error) {
                    console.error("❌ Erro ao alterar conferente:", error);
                    alert("Erro ao tentar alterar o conferente.");
                }
            }

            // 🗑️ Excluir conferente
            if (target.classList.contains("btnExcluir")) {
                if (!confirm("❓ Tem certeza que deseja excluir este conferente?")) return;

                try {
                    const response = await fetch(`/api/conferentes/${id}`, {
                        method: "DELETE",
                    });

                    if (response.ok) {
                        alert("🗑️ Conferente excluído com sucesso!");
                        const index = conferentes.findIndex((c) => c.id == id);
                        if (index !== -1) {
                            conferentes.splice(index, 1);
                            atualizarTabelaConferentes();
                        }
                    } else {
                        const errorMessage = await response.text();
                        alert(errorMessage || "Erro ao excluir o conferente.");
                    }
                } catch (error) {
                    console.error("❌ Erro ao excluir conferente:", error);
                    alert("Erro ao tentar excluir o conferente.");
                }
            }
        });

        // ✅ Carrega os conferentes ao iniciar a página
        async function carregarConferentes() {
            try {
                const response = await fetch("/api/conferentes");
                if (response.ok) {
                    const data = await response.json();
                    conferentes.length = 0;
                    conferentes.push(...data);

                    atualizarTabelaConferentes();
                } else {
                    alert("Erro ao carregar os conferentes.");
                }
            } catch (error) {
                console.error("❌ Erro ao carregar conferentes:", error);
                alert("Erro ao tentar carregar os conferentes.");
            }
        }

        carregarConferentes();

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
