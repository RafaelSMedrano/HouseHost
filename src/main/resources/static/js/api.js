export async function login(username, password) {
    const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    });

    const text = await response.text();
    return JSON.parse(text);
}

export async function registration(username, password, email) {
    const response = await fetch("http://localhost:8080/auth/registration", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password,
            email: email
        })
    });

    const text = await response.text();
    return JSON.parse(text);
}
