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
                    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
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

        // Elementos do Dashboard
        const conferenteSection = document.getElementById("conferenteSection");
        const lancarSection = document.getElementById("lancarSection");
        const historicoSection = document.getElementById("historicoSection");

        const conferenteButton = document.getElementById("btnConferente");
        const lancarButton = document.getElementById("btnLancar");
        const historicoButton = document.getElementById("btnHistorico");
        const sairButton = document.getElementById("btnSair");

        // Função para mostrar a seção correta e esconder as outras
        function mostrarSecao(secao) {
            [conferenteSection, lancarSection, historicoSection].forEach((s) => {
                s.style.display = s === secao ? "block" : "none";
            });
        }

        // Inicialmente esconde todas as seções
        mostrarSecao(null);

        // Evento para alternar a seção de Conferente
        if (conferenteButton) {
            conferenteButton.addEventListener("click", () => {
                mostrarSecao(conferenteSection);
            });
        }

        // Evento para alternar a seção de Lançar
        if (lancarButton) {
            lancarButton.addEventListener("click", () => {
                mostrarSecao(lancarSection);
            });
        }

        // Evento para alternar a seção de Histórico
        if (historicoButton) {
            historicoButton.addEventListener("click", () => {
                mostrarSecao(historicoSection);
            });
        }

        // Evento para o botão de Sair
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