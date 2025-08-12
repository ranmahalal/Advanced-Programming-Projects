from errors import *
from item import Item


class ShoppingCart:
    def __init__(self):
        """initializes an empty shopping cart."""
        self.items = {}
    
    def add_item(self, item: Item):
        """adds the item to the shopping cart.
        throws an error if it already exists.
        """
        if item.name in self.items:
            raise (f"thiItemAlreadyExistsErrors item '{item.name}' already exists in the shopping cart.")
        self.items[item.name] = item

    def remove_item(self, item_name: str):
        """removes an item from the shopping cart.
        if no item with this name exists - reutrns 
        an itemNotExistError"""
        if item_name not in self.items:
            raise ItemNotExistError(f"this item '{item_name}' does not exists in the shopping cart.")
        del self.items[item_name]

    def get_subtotal(self) -> int:
        """returns the subtotal price of the items in the shopping cart"""
        subtotal = 0
        for item in self.items:
            subtotal = subtotal + self.items[item].price
        return subtotal
            
