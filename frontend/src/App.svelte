<script>
  import { onMount } from 'svelte';
  import { api, setAuthToken, getAuthToken } from './lib/api';
  import Button from './lib/Button.svelte';
  import Input from './lib/Input.svelte';
  import Card from './lib/Card.svelte';
  import { Check, Trash2, Plus, LogOut } from 'lucide-svelte';

  let user = null;
  let todos = [];
  let loading = false;
  let error = null;
  let showLogin = true;
  let showRegister = false;

  // Auth form state
  let loginUsername = '';
  let loginPassword = '';
  let registerUsername = '';
  let registerEmail = '';
  let registerPassword = '';

  // Todo form state
  let newTodoTitle = '';
  let newTodoDescription = '';

  onMount(async () => {
    const token = getAuthToken();
    if (token) {
      await loadUser();
      await loadTodos();
    }
  });

  async function loadUser() {
    try {
      const result = await api.getMe();
      user = result.user;
      showLogin = false;
    } catch (err) {
      setAuthToken(null);
      user = null;
      showLogin = true;
    }
  }

  async function loadTodos() {
    try {
      loading = true;
      const result = await api.getTodos();
      todos = result.todos || [];
      error = null;
    } catch (err) {
      error = err.message;
    } finally {
      loading = false;
    }
  }

  async function handleLogin() {
    try {
      error = null;
      const result = await api.login(loginUsername, loginPassword);
      user = result.user;
      showLogin = false;
      loginUsername = '';
      loginPassword = '';
      await loadTodos();
    } catch (err) {
      error = err.message;
    }
  }

  async function handleRegister() {
    try {
      error = null;
      const result = await api.register(registerUsername, registerEmail, registerPassword);
      if (result.success) {
        // Auto login after registration
        await api.login(registerUsername, registerPassword);
        user = result.user;
        showLogin = false;
        showRegister = false;
        registerUsername = '';
        registerEmail = '';
        registerPassword = '';
        await loadTodos();
      }
    } catch (err) {
      error = err.message;
    }
  }

  async function handleLogout() {
    setAuthToken(null);
    user = null;
    todos = [];
    showLogin = true;
    showRegister = false;
  }

  async function handleCreateTodo() {
    if (!newTodoTitle.trim()) return;
    try {
      error = null;
      await api.createTodo(newTodoTitle, newTodoDescription);
      newTodoTitle = '';
      newTodoDescription = '';
      await loadTodos();
    } catch (err) {
      error = err.message;
    }
  }

  async function handleToggleTodo(todo) {
    try {
      error = null;
      await api.updateTodo(todo.id, { completed: !todo.completed });
      await loadTodos();
    } catch (err) {
      error = err.message;
    }
  }

  async function handleDeleteTodo(id) {
    try {
      error = null;
      await api.deleteTodo(id);
      await loadTodos();
    } catch (err) {
      error = err.message;
    }
  }
</script>

<div class="min-h-screen bg-gray-50">
  <div class="container mx-auto px-4 py-8 max-w-4xl">
    <div class="mb-8">
      <h1 class="text-4xl font-bold text-gray-900 mb-2">Clojure Todo App</h1>
      <p class="text-gray-600">A modern todo app built with Clojure and Svelte</p>
    </div>

    {#if error}
      <div class="mb-4 p-4 bg-red-50 border border-red-200 rounded-md text-red-800">
        {error}
      </div>
    {/if}

    {#if showLogin && !user}
      <Card className="p-6 mb-6">
        <div class="mb-4">
          <h2 class="text-2xl font-semibold mb-4">Login</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <Input
                bind:value={loginUsername}
                placeholder="Enter your username"
                on:keydown={(e) => e.key === 'Enter' && handleLogin()}
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <Input
                type="password"
                bind:value={loginPassword}
                placeholder="Enter your password"
                on:keydown={(e) => e.key === 'Enter' && handleLogin()}
              />
            </div>
            <Button on:click={handleLogin} className="w-full">Login</Button>
            <p class="text-center text-sm text-gray-600">
              Don't have an account?{' '}
              <button
                class="text-blue-600 hover:underline"
                on:click={() => { showRegister = true; showLogin = false; }}
              >
                Register
              </button>
            </p>
          </div>
        </div>
      </Card>
    {/if}

    {#if showRegister && !user}
      <Card className="p-6 mb-6">
        <div class="mb-4">
          <h2 class="text-2xl font-semibold mb-4">Register</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <Input
                bind:value={registerUsername}
                placeholder="Choose a username"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <Input
                type="email"
                bind:value={registerEmail}
                placeholder="Enter your email"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <Input
                type="password"
                bind:value={registerPassword}
                placeholder="Choose a password (min 8 characters)"
              />
            </div>
            <Button on:click={handleRegister} className="w-full">Register</Button>
            <p class="text-center text-sm text-gray-600">
              Already have an account?{' '}
              <button
                class="text-blue-600 hover:underline"
                on:click={() => { showLogin = true; showRegister = false; }}
              >
                Login
              </button>
            </p>
          </div>
        </div>
      </Card>
    {/if}

    {#if user}
      <Card className="p-6 mb-6">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h2 class="text-xl font-semibold">Welcome, {user.username}!</h2>
            <p class="text-sm text-gray-600">{user.email}</p>
          </div>
          <Button variant="outline" size="sm" on:click={handleLogout}>
            <LogOut class="w-4 h-4 mr-2" />
            Logout
          </Button>
        </div>
      </Card>

      <Card className="p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">Add New Todo</h3>
        <div class="space-y-3">
          <Input
            bind:value={newTodoTitle}
            placeholder="Todo title"
            on:keydown={(e) => e.key === 'Enter' && handleCreateTodo()}
          />
          <Input
            bind:value={newTodoDescription}
            placeholder="Description (optional)"
            on:keydown={(e) => e.key === 'Enter' && handleCreateTodo()}
          />
          <Button on:click={handleCreateTodo} className="w-full">
            <Plus class="w-4 h-4 mr-2" />
            Add Todo
          </Button>
        </div>
      </Card>

      <Card className="p-6">
        <h3 class="text-lg font-semibold mb-4">Your Todos</h3>
        {#if loading}
          <p class="text-gray-600">Loading...</p>
        {:else if todos.length === 0}
          <p class="text-gray-600">No todos yet. Create one above!</p>
        {:else}
          <div class="space-y-3">
            {#each todos as todo (todo.id)}
              <div
                class="flex items-start gap-3 p-4 border border-gray-200 rounded-md hover:bg-gray-50"
                class:bg-gray-50={todo.completed}
              >
                <button
                  on:click={() => handleToggleTodo(todo)}
                  class="mt-1 flex-shrink-0"
                >
                  <div
                    class="w-5 h-5 rounded border-2 flex items-center justify-center"
                    class:bg-blue-600={todo.completed}
                    class:border-blue-600={todo.completed}
                    class:border-gray-300={!todo.completed}
                  >
                    {#if todo.completed}
                      <Check class="w-3 h-3 text-white" />
                    {/if}
                  </div>
                </button>
                <div class="flex-1 min-w-0">
                  <h4
                    class="font-medium"
                    class:line-through={todo.completed}
                    class:text-gray-500={todo.completed}
                  >
                    {todo.title}
                  </h4>
                  {#if todo.description}
                    <p
                      class="text-sm text-gray-600 mt-1"
                      class:line-through={todo.completed}
                      class:text-gray-400={todo.completed}
                    >
                      {todo.description}
                    </p>
                  {/if}
                </div>
                <button
                  on:click={() => handleDeleteTodo(todo.id)}
                  class="flex-shrink-0 p-2 text-red-600 hover:bg-red-50 rounded"
                >
                  <Trash2 class="w-4 h-4" />
                </button>
              </div>
            {/each}
          </div>
        {/if}
      </Card>
    {/if}
  </div>
</div>

