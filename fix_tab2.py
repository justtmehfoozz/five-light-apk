with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "r") as f:
    content = f.read()

tabs_old = """                } else {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {"""

tabs_new = """                } else if (selectedTab == 1) {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {"""

content = content.replace(tabs_old, tabs_new)

tabs_old2 = """                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BismillahHeader("""

tabs_new2 = """                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    com.example.ui.components.QuranPlannerTabContent()
                }
            }
        }
    }
}

@Composable
fun BismillahHeader("""

content = content.replace(tabs_old2, tabs_new2)

with open("app/src/main/java/com/example/ui/screens/QuranScreen.kt", "w") as f:
    f.write(content)
