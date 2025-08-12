from shopping_cart import ShoppingCart
from store import Store
from item import Item

def main():
    store = Store('items.yml)')
    print("store items are:")
    for item in store.get_items():
        print(item)
