from typing import Dict, Any
from errors import ItemNotExistError, InvalidQuantityError
from item import Item

class ShoppingCart:
    """Shopping cart that stores items with quantities."""
    
    def __init__(self):
        """Initialize empty cart."""
        # format: {item_name: {'item': Item, 'quantity': int}}
        self._items = {}
    
    @property
    def items(self) -> Dict[str, Dict[str, Any]]:
        """Get cart items."""
        return self._items
    
    def add_item(self, item: Item, quantity: int = 1) -> None:
        """
        Add item to cart or increase quantity if exists.
        Args:
            item: Item to add
            quantity: Quantity to add
        """
        if quantity <= 0:
            raise InvalidQuantityError(f"Quantity must be positive, got: {quantity}")
        
        if item.name in self._items:
            # Item exists, increase quantity
            self._items[item.name]['quantity'] += quantity
        else:
            # New item
            self._items[item.name] = {
                'item': item,
                'quantity': quantity
            }

    def remove_item(self, item_name: str, quantity: int = None) -> None:
        """
        Remove item from cart.
        Args:
            item_name: Name of item to remove
            quantity: Quantity to remove (None = remove all)
        """
        if item_name not in self._items:
            raise ItemNotExistError(f"Item '{item_name}' not in cart")
        
        if quantity is None:
            # Remove entire item
            del self._items[item_name]
        else:
            if quantity <= 0:
                raise InvalidQuantityError(f"Quantity must be positive, got: {quantity}")
            
            current_qty = self._items[item_name]['quantity']
            if quantity >= current_qty:
                # Remove entire item
                del self._items[item_name]
            else:
                # Reduce quantity
                self._items[item_name]['quantity'] -= quantity

    def get_subtotal(self) -> float:
        """Calculate total price of items in cart."""
        total = 0.0
        for item_data in self._items.values():
            item = item_data['item']
            quantity = item_data['quantity']
            total += item.price * quantity
        return total
    
    def get_total_items(self) -> int:
        """Get total number of items (including quantities)."""
        return sum(item_data['quantity'] for item_data in self._items.values())
    
    def is_empty(self) -> bool:
        """Check if cart is empty."""
        return len(self._items) == 0
    
    def clear(self) -> None:
        """Clear all items from cart."""
        self._items.clear()
    
    def has_item(self, item_name: str) -> bool:
        """Check if item is in cart."""
        return item_name in self._items