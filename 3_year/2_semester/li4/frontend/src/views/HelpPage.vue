<template>
  <div class="h-full bg-gray-50 p-6 overflow-y-auto">
    <div class="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Ajuda Rápida</h1>
        <p class="text-sm text-gray-600 mt-1">Guia resumido para operar o sistema com formação curta.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <section class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-lg font-semibold text-gray-900 mb-3">Operações diárias</h2>
          <ol class="text-sm text-gray-700 space-y-2 list-decimal list-inside">
            <li>Aceda ao <strong>Ponto de Venda</strong> e registe os artigos.</li>
            <li>Finalize o pagamento em numerário ou cartão.</li>
            <li>Consulte o <strong>Dashboard</strong> para validar resultados.</li>
          </ol>
        </section>

        <section class="bg-white rounded-lg border border-gray-200 p-5">
          <h2 class="text-lg font-semibold text-gray-900 mb-3">Gestão rápida</h2>
          <ol class="text-sm text-gray-700 space-y-2 list-decimal list-inside">
            <li>Abra <strong>Produtos</strong> para atualizar catálogo e promoções.</li>
            <li>Abra <strong>Fornecedores</strong> para manter parceiros ativos.</li>
            <li>Abra <strong>Encomendas</strong> para gerir o pipeline de abastecimento.</li>
            <li>Abra <strong>Funcionários</strong> para gerir contas e perfis.</li>
          </ol>
        </section>
      </div>

      <section class="bg-white rounded-lg border border-gray-200 p-5">
        <h2 class="text-lg font-semibold text-gray-900 mb-3">Linguagem e mensagens</h2>
        <p class="text-sm text-gray-700">
          Todas as mensagens operacionais usam linguagem simples em português (pt-PT). Em caso de erro de segurança ou acesso,
          o sistema bloqueia a operação e apresenta instruções claras sobre o próximo passo.
        </p>
      </section>
    </div>
  </div>
</template>
