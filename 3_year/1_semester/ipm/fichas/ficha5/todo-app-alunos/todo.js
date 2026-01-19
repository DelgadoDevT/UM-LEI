const todos = [
  'Learn a new course',
  'Read a book',
  'Go to the gym',
  'Go shopping'
];

/* Ex. 1: Add a event listener that triggers when the DOM is loaded here */
document.addEventListener('DOMContentLoaded', () => {
    const dataElement = document.getElementById("list-date");
    const today = new Date();
    dataElement.textContent = today.toLocaleDateString('pt-PT');

    renderTodoList();
});


/* Ex. 2: Complete todo rendering */
// complete function to remove all child nodes
// arg parent is the node to clean
function removeAllChildNodes(parent) {
    while (parent.firstChild) {
        parent.firstChild.querySelector("button").removeEventListener("click", removeTodoItem);
        parent.removeChild(parent.firstChild);
    }
}

// render todo array here
function renderTodoList() {
    const todoList = document.getElementById('todo-list');

    todos.forEach(todo => {
        const todoItem = document.createElement("li");
        const todoText = document.createElement("p");
        const todoButton = document.createElement("button");

        todoText.textContent = todo;
        todoButton.textContent = 'Delete';
        todoButton.addEventListener('click', removeTodoItem);
        todoItem.classList.add("todo-list-item");

        todoItem.appendChild(todoText);
        todoItem.appendChild(todoButton);
        todoList.appendChild(todoItem);
    });
}



/* Ex. 3: Add a event listener to element 'todo-form'*/
document.getElementById('todo-form').addEventListener('submit', event => {
    event.preventDefault();
    const taskInput  = document.getElementById('task-input');
    const todoValue = taskInput.value;
    taskInput.value = '';

    if (todoValue === '') 
        return;

    if (todos.includes(todoValue)) {
        alert("Erro! " + todoValue + " já existe!");
        return;
    }

    removeAllChildNodes(document.getElementById('todo-list'));
    todos.push(todoValue);
    renderTodoList();
});



/* Ex. 4 and 5: complete delete button click logic */
// arg event is the triggered event (with event you can get the element clicked).
function removeTodoItem(event) {
    const target = event.target;
    const listItem = target.parentNode;
    const todo = listItem.getElementsByTagName('p')[0].textContent;
    const todoIndex =todos.indexOf(todo);

    todos.splice(todoIndex, 1);
    removeAllChildNodes(document.getElementById('todo-list'));
    renderTodoList();
}