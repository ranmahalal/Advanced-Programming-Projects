class ItemNotExistError(Exception):
    """Raised when an item doesn't exist."""
    pass


class TooManyMatchesError(Exception):
    """Raised when search matches too many items."""
    pass


class InsufficientStockError(Exception):
    """Raised when not enough stock is available."""
    pass


class InvalidQuantityError(Exception):
    """Raised when quantity is invalid."""
    pass