<script>
  import { cn } from './utils';

  export let className = '';
</script>

<div class={cn('rounded-lg border border-gray-200 bg-white shadow-sm', className)}>
  <slot />
</div>

