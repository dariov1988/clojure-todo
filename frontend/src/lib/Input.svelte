<script>
  import { cn } from './utils';

  export let type = 'text';
  export let placeholder = '';
  export let value = '';
  export let className = '';
  export let disabled = false;

  function handleInput(event) {
    value = event.target.value;
  }
</script>

<input
  {type}
  {placeholder}
  {value}
  {disabled}
  class={cn(
    'flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-white file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
    className
  )}
  on:input={handleInput}
  on:change={handleInput}
/>

