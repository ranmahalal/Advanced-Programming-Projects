from typing import List

class Item:
    """Represents an item in the store."""
    
    def __init__(self, item_name: str, item_price: float, 
                 item_description: str, stock: int):
        """
        Initialize an item.
        Args:
            item_name: Name of the item
            item_price: Price of the item  
            item_description: Description of the item
            stock: Available stock
        """
        self.name = item_name
        self.price = item_price
        self.description = item_description
        self.stock = stock

    def __str__(self) -> str:
        """String representation of the item."""
        return f'Name:\t\t\t{self.name}\n' \
               f'Price:\t\t\t${self.price:.2f}\n' \
               f'Stock:\t\t\t{self.stock} units\n' \
               f'Description:\t{self.description}'
    
    def has_stock(self, quantity: int = 1) -> bool:
        """Check if item has enough stock."""
        return self.stock >= quantity
    
    def reduce_stock(self, quantity: int) -> None:
        """Reduce stock by quantity."""
        if not self.has_stock(quantity):
            raise ValueError(f"Not enough stock. Available: {self.stock}, requested: {quantity}")
        self.stock -= quantity
    
    def add_stock(self, quantity: int) -> None:
        """Add stock."""
        self.stock += quantity