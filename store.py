import yaml
from typing import List, Dict, Any
from item import Item
from shopping_cart import ShoppingCart
from errors import *


class Store:
    """Main store class with inventory and cart management."""
    
    def __init__(self, path: str):
        """Initialize store with items from file."""
        with open(path, 'r') as inventory:
            items_raw = yaml.safe_load(inventory)['items']
        self._items = self._convert_to_item_objects(items_raw)
        self._shopping_cart = ShoppingCart()

    @staticmethod
    def _convert_to_item_objects(items_raw: List[Dict]) -> List[Item]:
        """Convert raw item data to Item objects."""
        items = []
        for item_data in items_raw:
            item = Item(
                item_data['name'],
                float(item_data['price']),
                item_data['description'],
                item_data['stock']
            )
            items.append(item)
        return items
    
    def __str__(self) -> str:
        """String representation of the store showing all items with name and price."""
        if not self._items:
            return "Store is empty"
        
        # Create formatted strings for each item
        item_strings = [f"{item.name}: ${item.price:.2f} (Stock: {item.stock})" for item in self._items]
        
        # Join multiple items per line (e.g., 3 items per line)
        lines = []
        items_per_line = 3
        
        for i in range(0, len(item_strings), items_per_line):
            line_items = item_strings[i:i + items_per_line]
            # Pad each item to consistent width for better alignment
            formatted_items = [f"{item:<35}" for item in line_items]
            lines.append(" | ".join(formatted_items))
    
        return f"Store Items ({len(self._items)} total):\n" + "\n".join(lines)

    def get_items(self) -> List[Item]:
        """Get all store items."""
        return self._items

    def search_by_name(self, item_name: str) -> List[Item]:
        """Search items by name, excluding cart items."""
        matches = []
        for item in self._items:
            if (item_name.lower() in item.name.lower() and 
                not self._shopping_cart.has_item(item.name) and
                item.stock > 0):
                matches.append(item)
        
        # Sort by name (asc)
        matches.sort(key=lambda x: x.name.lower())
        return matches

    def add_item(self, item_name: str, quantity: int = 1) -> Dict[str, Any]:
        """
        Add item to cart with stock management.
        
        Returns:
            Dict with success status and message
        """
        try:
            # Find matching items
            matches = [item for item in self._items 
                      if item_name.lower() in item.name.lower()]
            
            if len(matches) == 0:
                raise ItemNotExistError(f"Item '{item_name}' not found")
            
            if len(matches) > 1:
                item_names = [item.name for item in matches]
                raise TooManyMatchesError(
                    f"'{item_name}' matches multiple items: {', '.join(item_names)}"
                )
            
            item = matches[0]
            
            # Check stock (including what's already in cart)
            cart_quantity = 0
            if self._shopping_cart.has_item(item.name):
                cart_quantity = self._shopping_cart.items[item.name]['quantity']
            
            if not item.has_stock(quantity):
                raise InsufficientStockError(
                    f"Not enough stock for '{item.name}'. "
                    f"Available: {item.stock}, in cart: {cart_quantity}, requested: {quantity}"
                )
            
            # Add to cart and reduce stock
            self._shopping_cart.add_item(item, quantity)
            item.reduce_stock(quantity)
            
            return {
                'success': True,
                'message': f"Added {quantity}x '{item.name}' to cart"
            }
            
        except Exception as e:
            return {
                'success': False,
                'message': str(e)
            }

    def remove_item(self, item_name: str, quantity: int = None) -> Dict[str, Any]:
        """
        Remove item from cart and restore stock.
        
        Returns:
            Dict with success status and message
        """
        try:
            # Find matching items in store
            matches = [item for item in self._items 
                      if item_name.lower() in item.name.lower()]
            
            if len(matches) == 0:
                raise ItemNotExistError(f"Item '{item_name}' not found")
            
            if len(matches) > 1:
                item_names = [item.name for item in matches]
                raise TooManyMatchesError(
                    f"'{item_name}' matches multiple items: {', '.join(item_names)}"
                )
            
            item = matches[0]
            
            if not self._shopping_cart.has_item(item.name):
                raise ItemNotExistError(f"'{item.name}' not in cart")
            
            # Get quantity to remove
            if quantity is None:
                remove_qty = self._shopping_cart.items[item.name]['quantity']
            else:
                current_qty = self._shopping_cart.items[item.name]['quantity']
                #if requested quantity is biggee the cart quantity, remove the cart quantity.
                remove_qty = min(quantity, current_qty)
            
            # Remove from cart and restore stock
            self._shopping_cart.remove_item(item.name, quantity)
            item.add_stock(remove_qty)
            
            return {
                'success': True,
                'message': f"Removed {remove_qty}x '{item.name}' from cart"
            }
            
        except Exception as e:
            return {
                'success': False,
                'message': str(e)
            }

    def checkout(self) -> Dict[str, Any]:
        """
        Process checkout and clear cart.
        
        Returns:
            Dict with checkout details
        """
        if self._shopping_cart.is_empty():
            return {
                'success': False,
                'message': "Cart is empty"
            }
        
        total = self._shopping_cart.get_subtotal()
        item_count = self._shopping_cart.get_total_items()
        
        # Clear cart (stock already reduced when items were added)
        self._shopping_cart.clear()
        
        return {
            'success': True,
            'message': f"Checkout successful! Total: ${total:.2f} ({item_count} items)",
            'total': total,
            'item_count': item_count
        }

    def show_cart(self) -> None:
        """Display cart contents."""
        if self._shopping_cart.is_empty():
            print("Your cart is empty.")
            return
        
        print("\n=== Your Shopping Cart ===")
        total_items = 0
        for item_data in self._shopping_cart.items.values():
            item = item_data['item']
            quantity = item_data['quantity']
            subtotal = item.price * quantity
            total_items += quantity
            print(f"{item.name} x{quantity} - ${subtotal:.2f}")
        
        print(f"\nTotal items: {total_items}")
        print(f"Total price: ${self._shopping_cart.get_subtotal():.2f}")
        print("=" * 30)