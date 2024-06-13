

// Traductions françaises pour Leaflet.draw
L.drawLocal = {
    draw: {
        toolbar: {
            actions: {
                title: 'Annuler le dessin',
                text: 'Annuler'
            },
            finish: {
                title: 'Terminer le dessin',
                text: 'Terminer'
            },
            undo: {
                title: 'Supprimer le dernier point dessiné',
                text: 'Supprimer le dernier point'
            },
            buttons: {
                polyline: 'Dessiner une polyligne',
                polygon: 'Dessiner un polygone',
                rectangle: 'Dessiner un rectangle',
                circle: 'Dessiner un cercle',
                marker: 'Dessiner un marqueur',
                circlemarker: 'Dessiner un marqueur circulaire'
            }
        },
        handlers: {
            circle: {
                tooltip: {
                    start: 'Cliquez et déplacez pour dessiner un cercle.'
                },
                radius: 'Rayon'
            },
            circlemarker: {
                tooltip: {
                    start: 'Cliquez sur la carte pour placer un marqueur circulaire.'
                }
            },
            marker: {
                tooltip: {
                    start: 'Cliquez sur la carte pour placer un marqueur.'
                }
            },
            polygon: {
                error: '<strong>Erreur:</strong> les bords du polygone ne doivent pas se croiser!',
                tooltip: {
                    start: 'Cliquez pour commencer à dessiner une forme.',
                    cont: 'Cliquez pour continuer à dessiner la forme.',
                    end: 'Cliquez sur le premier point pour fermer cette forme.'
                }
            },
            polyline: {
                error: '<strong>Erreur:</strong> les bords ne doivent pas se croiser!',
                tooltip: {
                    start: 'Cliquez pour commencer à dessiner une ligne.',
                    cont: 'Cliquez pour continuer à dessiner la ligne.',
                    end: 'Cliquez sur le dernier point pour terminer la ligne.'
                }
            },
            rectangle: {
                tooltip: {
                    start: 'Cliquez et déplacez pour dessiner un rectangle.'
                }
            },
            simpleshape: {
                tooltip: {
                    end: 'Relâchez la souris pour terminer le dessin.'
                }
            }
        }
    },
    edit: {
        toolbar: {
            actions: {
                save: {
                    title: 'Enregistrer les modifications',
                    text: 'Enregistrer'
                },
                cancel: {
                    title: 'Annuler les modifications',
                    text: 'Annuler'
                },
                clearAll: {
                    title: 'Effacer toutes les couches',
                    text: 'Tout effacer'
                }
            },
            buttons: {
                edit: 'Modifier les couches',
                editDisabled: 'Aucune couche à modifier',
                remove: 'Supprimer les couches',
                removeDisabled: 'Aucune couche à supprimer'
            }
        },
        handlers: {
            edit: {
                tooltip: {
                    text: 'Faites glisser les poignées ou les marqueurs pour modifier la forme.',
                    subtext: 'Cliquez sur annuler pour annuler les modifications.'
                }
            },
            remove: {
                tooltip: {
                    text: 'Cliquez sur une forme pour la supprimer.'
                }
            }
        }
    }
};