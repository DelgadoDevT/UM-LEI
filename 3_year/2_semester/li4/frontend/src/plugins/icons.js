import {
  Euro, ShoppingCart, Package, TrendingUp, TrendingDown, Banknote, BarChart3,
  AlertCircle, ShoppingBag, LayoutDashboard, Truck, Users, FileText, LineChart,
  User, LogOut, HelpCircle, Wifi, WifiOff,

  // Ícones que faltavam para o Ponto de Venda:
  Search, Plus, Trash2, X, CreditCard, Wallet, Minus, Keyboard, Undo
} from 'lucide-vue-next';

export const icons = {
  Euro, ShoppingCart, Package, TrendingUp, TrendingDown, Banknote, BarChart3,
  AlertCircle, ShoppingBag, LayoutDashboard, Truck, Users, FileText, LineChart,
  User, LogOut, HelpCircle, Wifi, WifiOff,

  // Registar com nomes blindados contra erros de HTML:
  SearchIcon: Search,
  PlusIcon: Plus,
  CartIcon: ShoppingCart,
  TrashIcon: Trash2,
  XIcon: X,
  CardIcon: CreditCard,
  WalletIcon: Wallet,
  MinusIcon: Minus,
  KeyboardIcon: Keyboard,
  UndoIcon: Undo
};

export default {
  install(app) {
    for (const [name, component] of Object.entries(icons)) {
      app.component(name, component);
    }
  }
};